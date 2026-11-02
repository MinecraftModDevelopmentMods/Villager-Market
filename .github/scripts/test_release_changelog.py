"""Release-note and archival checks; run with Python 3 and Bash, without uploads."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


SCRIPTS = Path(__file__).resolve().parent
BASH = os.environ.get("BASH_EXECUTABLE", "bash")
HISTORY = """## 0.0.3-beta

- Beta fixes.

## 0.0.2-alpha

- Alpha additions.
"""
OLDER = """
## 0.0.1

- Previous stable changes.

## 0.0.1-alpha

- Older alpha changes.
"""


class ReleaseChangelogTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="release-notes-test-")
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.changelog = self.root / "CHANGELOG.md"

    def write_changelog(self, new="- New changes.\n", history=HISTORY + OLDER):
        self.changelog.write_text(
            "# Changelog\n\nIntroduction.\n\n## Unreleased\n\n" + new + "\n" + history,
            encoding="utf-8",
        )

    def run_script(self, script, *args, success=True):
        result = subprocess.run(
            [BASH, (SCRIPTS / script).as_posix(), *[
                arg.as_posix() if isinstance(arg, Path) else arg for arg in args
            ]], capture_output=True, text=True,
        )
        if success:
            self.assertEqual(result.returncode, 0, result.stderr)
        else:
            self.assertNotEqual(result.returncode, 0)
        return result.stdout

    def notes(self, version, success=True):
        return self.run_script("release-changelog.sh", "notes", self.changelog, version, success=success)

    def test_prereleases_only_include_unreleased(self):
        self.write_changelog()
        for version in ("0.0.4-alpha", "0.0.4-beta"):
            with self.subTest(version=version):
                self.assertEqual(self.notes(version), "- New changes.\n")

    def test_first_stable_includes_all_prereleases_by_version(self):
        self.write_changelog(history=HISTORY)
        self.assertEqual(self.notes("0.0.4"), "## 0.0.4\n\n- New changes.\n\n" + HISTORY)

    def test_stable_stops_before_previous_stable(self):
        self.write_changelog()
        self.assertEqual(self.notes("0.0.4"), "## 0.0.4\n\n- New changes.\n\n" + HISTORY)

    def test_stable_without_prereleases(self):
        self.write_changelog(history=OLDER.lstrip())
        self.assertEqual(self.notes("0.0.4"), "## 0.0.4\n\n- New changes.\n")

    def test_stable_without_any_history(self):
        self.write_changelog(history="")
        self.assertEqual(self.notes("0.0.1"), "## 0.0.1\n\n- New changes.\n")

    def test_promotion_archives_empty_stable_cutoff(self):
        self.write_changelog(new="")
        self.assertEqual(self.notes("0.0.4"), HISTORY)
        tagged = self.root / "tagged.md"
        tagged.write_bytes(self.changelog.read_bytes())
        properties = self.root / "gradle.properties"
        properties.write_text("version=0.0.4\n", encoding="utf-8")
        self.run_script("release-bump.sh", "0.0.4", "0.0.5", self.changelog, tagged, properties)
        self.assertEqual(properties.read_text(), "version=0.0.5\n")
        archived = self.changelog.read_text()
        self.assertIn("## Unreleased\n\n## 0.0.4\n", archived)
        self.assertEqual(archived.count("- Beta fixes."), 1)
        self.assertEqual(archived.count("- Alpha additions."), 1)
        self.changelog.write_text(archived.replace("## Unreleased\n", "## Unreleased\n\n- Next cycle.\n"), encoding="utf-8")
        self.assertEqual(self.notes("0.0.5"), "## 0.0.5\n\n- Next cycle.\n")

    def test_stable_bump_only_archives_new_notes(self):
        self.write_changelog()
        self.run_script("release-changelog.sh", "bump", self.changelog, "0.0.4")
        archived = self.changelog.read_text()
        self.assertIn("## 0.0.4\n\n- New changes.\n\n## 0.0.3-beta", archived)
        self.assertEqual(archived.count("- Beta fixes."), 1)

    def test_empty_prereleases_and_empty_stable_cycle_fail(self):
        self.write_changelog(new="")
        for version in ("0.0.4-alpha", "0.0.4-beta"):
            self.notes(version, success=False)
        self.write_changelog(new="", history=OLDER.lstrip())
        self.notes("0.0.4", success=False)

    def test_invalid_changelog_or_version_fails(self):
        for heading in ("Typo", "0x0x3-alpha"):
            self.write_changelog(history=f"## {heading}\n\n- Changes.\n")
            self.notes("0.0.4", success=False)
        self.notes("invalid", success=False)
        self.changelog.write_text("# No Unreleased section\n", encoding="utf-8")
        self.notes("0.0.4", success=False)

    def test_large_accumulated_history(self):
        history = "## 0.0.3-beta\n\n" + "- An earlier change.\n" * 10000
        self.write_changelog(history=history + OLDER)
        self.assertEqual(self.notes("0.0.4"), "## 0.0.4\n\n- New changes.\n\n" + history)


if __name__ == "__main__":
    unittest.main()
