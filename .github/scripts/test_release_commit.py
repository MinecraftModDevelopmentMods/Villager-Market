"""Exercise direct release commits against temporary local Git repositories."""
import os
from pathlib import Path
import shlex
import shutil
import subprocess
import tempfile
import unittest


SCRIPTS = Path(__file__).resolve().parent
BASH = os.environ.get("BASH_EXECUTABLE", "bash")


class ReleaseCommitTest(unittest.TestCase):
    def git(self, directory, *args):
        result = subprocess.run(["git", "-C", str(directory), *args], capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stderr)
        return result.stdout.strip()

    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="release-commit-test-")
        self.addCleanup(self.temp.cleanup)
        root = Path(self.temp.name)
        self.remote = root / "remote.git"
        self.writer = root / "writer"
        self.runner = root / "runner"
        self.git(root, "init", "--bare", str(self.remote))
        self.git(root, "init", "--initial-branch=main", str(self.writer))
        self.git(self.writer, "config", "user.name", "Release test")
        self.git(self.writer, "config", "user.email", "release-test@example.invalid")
        self.git(self.writer, "config", "core.autocrlf", "false")
        scripts = self.writer / ".github/scripts"
        scripts.mkdir(parents=True)
        for source in SCRIPTS.glob("release-*.sh"):
            shutil.copyfile(source, scripts / source.name)
        (self.writer / "gradle.properties").write_text("version=0.0.1\n", encoding="utf-8")
        (self.writer / "CHANGELOG.md").write_text("# Changelog\n\n## Unreleased\n\n- First changes.\n", encoding="utf-8")
        self.git(self.writer, "add", ".")
        self.git(self.writer, "commit", "-m", "Initial release")
        self.git(self.writer, "tag", "0.0.1-alpha")
        self.git(self.writer, "remote", "add", "origin", str(self.remote))
        self.git(self.writer, "push", "origin", "main", "0.0.1-alpha")
        self.git(root, "clone", "--branch", "main", str(self.remote), str(self.runner))
        self.git(self.runner, "checkout", "--detach", "0.0.1-alpha")

    def bump(self, success=True, ci_exit="0"):
        env = dict(os.environ, TEST_CI_TRACE=(self.runner.parent / "ci-trace.txt").as_posix(), TEST_CI_EXIT=ci_exit)
        harness = r'''
 gh() {
  printf '%s\n' "$*" >> "$TEST_CI_TRACE"
  case "$1 $2" in
    "workflow run") return 0 ;;
    "run list") echo 123 ;;
    "run watch") return "$TEST_CI_EXIT" ;;
    *) echo "Unexpected gh call" >&2; return 1 ;;
  esac
}
export -f gh
bash .github/scripts/release-commit.sh 0.0.1-alpha 0.0.2 0.0.1-alpha
'''
        result = subprocess.run(
            [BASH, "-c", harness],
            cwd=self.runner, env=env, capture_output=True, text=True,
        )
        if success:
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        else:
            self.assertNotEqual(result.returncode, 0)
        return result

    def test_direct_commit_and_idempotent_rerun(self):
        tagged = self.git(self.remote, "rev-parse", "0.0.1-alpha")
        self.bump()
        committed = self.git(self.remote, "rev-parse", "main")
        self.assertNotEqual(committed, tagged)
        self.assertEqual(self.git(self.remote, "show", "main:gradle.properties"), "version=0.0.2")
        self.assertIn("## Unreleased\n\n## 0.0.1-alpha", self.git(self.remote, "show", "main:CHANGELOG.md"))
        self.assertEqual(self.git(self.remote, "rev-parse", "0.0.1-alpha"), tagged)
        self.assertEqual(self.git(self.remote, "for-each-ref", "--format=%(refname)", "refs/heads"), "refs/heads/main")
        self.bump()
        self.assertEqual(self.git(self.remote, "rev-parse", "main"), committed)

    def test_failed_ci_leaves_main_unchanged(self):
        original = self.git(self.remote, "rev-parse", "main")
        self.bump(success=False, ci_exit="1")
        self.assertEqual(self.git(self.remote, "rev-parse", "main"), original)
        trace = (self.runner.parent / "ci-trace.txt").read_text()
        self.assertIn("workflow run ci.yml --ref release-next/", trace)
        self.assertIn("run watch 123 --exit-status", trace)

    def test_newer_main_notes_are_preserved(self):
        changelog = self.writer / "CHANGELOG.md"
        changelog.write_text(changelog.read_text().replace("## Unreleased\n", "## Unreleased\n\n- Added during build.\n"), encoding="utf-8")
        self.git(self.writer, "add", "CHANGELOG.md")
        self.git(self.writer, "commit", "-m", "Concurrent changes")
        self.git(self.writer, "push", "origin", "main")
        self.bump()
        self.assertEqual(self.git(self.remote, "show", "main:CHANGELOG.md"), changelog.read_text().strip())
        self.assertEqual(self.git(self.remote, "show", "main:gradle.properties"), "version=0.0.2")

    def test_push_race_is_rejected_and_rerun_preserves_concurrent_commit(self):
        (self.writer / "concurrent.txt").write_text("Keep this change.\n", encoding="utf-8")
        self.git(self.writer, "add", "concurrent.txt")
        self.git(self.writer, "commit", "-m", "Concurrent commit")
        concurrent = self.git(self.writer, "rev-parse", "HEAD")
        # Advance remote main after the release script fetches, just before its push.
        hook = self.runner / ".git/hooks/pre-push"
        hook.write_text(
            "#!/usr/bin/env bash\nset -eu\n"
            + "env -u GIT_DIR -u GIT_WORK_TREE git -C " + shlex.quote(self.writer.as_posix())
            + ' push origin main\nrm -- "$0"\n', encoding="utf-8",
        )
        hook.chmod(0o755)
        self.bump(success=False)
        self.assertEqual(self.git(self.remote, "rev-parse", "main"), concurrent)
        self.bump()
        self.assertEqual(self.git(self.remote, "rev-parse", "main^"), concurrent)
        self.assertEqual(self.git(self.remote, "show", "main:concurrent.txt"), "Keep this change.")


if __name__ == "__main__":
    unittest.main()
