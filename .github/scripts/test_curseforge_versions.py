"""Version catalog captured on 2026-11-02, with synthetic environment IDs."""
import contextlib
import io
import json
import os
from pathlib import Path
import runpy
import subprocess
import sys
import tempfile
import unittest


resolve = runpy.run_path(str(Path(__file__).with_name("curseforge-version-ids.py")))["resolve"]

TYPES = [
    {"id": 2, "slug": "java"},
    {"id": 615, "slug": "addons"},
    {"id": 75208, "slug": "environment"},
    {"id": 68441, "slug": "modloader"},
    {"id": 77784, "slug": "minecraft-1-21"},
    {"id": 88556, "slug": "minecraft-26-3"},
]
VERSIONS = [
    {"id": 99901, "gameVersionTypeID": 75208, "name": "Client"},
    {"id": 99902, "gameVersionTypeID": 75208, "name": "Server"},
    {"id": 7499, "gameVersionTypeID": 68441, "name": "Fabric"},
    {"id": 10150, "gameVersionTypeID": 68441, "name": "NeoForge"},
    {"id": 11135, "gameVersionTypeID": 2, "name": "Java 21"},
    {"id": 11779, "gameVersionTypeID": 77784, "name": "1.21.1"},
    {"id": 12735, "gameVersionTypeID": 1, "name": "1.21.1"},
    {"id": 14454, "gameVersionTypeID": 2, "name": "Java 25"},
    {"id": 16115, "gameVersionTypeID": 615, "name": "1.21.1"},
    {"id": 17045, "gameVersionTypeID": 88556, "name": "26.3"},
    {"id": 17046, "gameVersionTypeID": 1, "name": "26.3"},
]


class CurseForgeVersionsTest(unittest.TestCase):
    def test_actual_duplicate_names_resolve_for_all_three_jars(self):
        for game, loader, java, expected in (
            ("1.21.1", "NeoForge", "Java 21", [11779, 10150, 99901, 99902, 11135]),
            ("26.3", "Fabric", "Java 25", [17045, 7499, 99901, 99902, 14454]),
            ("26.3", "NeoForge", "Java 25", [17045, 10150, 99901, 99902, 14454]),
        ):
            with self.subTest(game=game, loader=loader):
                self.assertEqual(resolve(VERSIONS, TYPES, game, loader, java), expected)

    def test_data_envelopes(self):
        self.assertEqual(resolve({"data": VERSIONS}, {"data": TYPES}, "26.3", "Fabric", "Java 25"), [17045, 7499, 99901, 99902, 14454])

    def test_wrong_categories_are_not_used_as_fallback(self):
        versions = [row for row in VERSIONS if row["id"] != 11779]
        with self.assertRaisesRegex(ValueError, "minecraft-1-21"):
            resolve(versions, TYPES, "1.21.1", "NeoForge", "Java 21")

    def test_ambiguous_id_within_correct_category_fails(self):
        versions = VERSIONS + [{"id": 99999, "gameVersionTypeID": 77784, "name": "1.21.1"}]
        with self.assertRaisesRegex(ValueError, "2 versions"):
            resolve(versions, TYPES, "1.21.1", "NeoForge", "Java 21")

    def test_missing_or_ambiguous_category_fails(self):
        for types in (
            [row for row in TYPES if row["slug"] != "modloader"],
            TYPES + [{"id": 99999, "slug": "modloader"}],
        ):
            with self.assertRaisesRegex(ValueError, "modloader"):
                resolve(VERSIONS, types, "26.3", "Fabric", "Java 25")

    def test_optional_java_can_be_absent(self):
        with contextlib.redirect_stderr(io.StringIO()) as warning:
            self.assertEqual(resolve(VERSIONS, TYPES, "26.3", "Fabric", "Java 99"), [17045, 7499, 99901, 99902])
        self.assertIn("omitting optional Java", warning.getvalue())

    def test_empty_catalog_fails(self):
        with self.assertRaisesRegex(ValueError, "empty or invalid"):
            resolve([], TYPES, "26.3", "Fabric", "Java 25")

    def test_required_environment_is_not_silently_omitted(self):
        versions = [row for row in VERSIONS if row["name"] != "Server"]
        with self.assertRaisesRegex(ValueError, "environment"):
            resolve(versions, TYPES, "26.3", "Fabric", "Java 25")

    def test_publisher_preflight_and_resolved_upload_metadata(self):
        for valid in (False, True):
            with self.subTest(valid=valid), tempfile.TemporaryDirectory(prefix="curseforge-test-") as folder:
                root = Path(folder)
                versions = VERSIONS if valid else [row for row in VERSIONS if row["id"] != 17045]
                (root / "versions.json").write_text(json.dumps(versions), encoding="utf-8")
                (root / "types.json").write_text(json.dumps(TYPES), encoding="utf-8")
                (root / "notes.md").write_text("- Release notes.\n", encoding="utf-8")
                for target in ("neoforge-1.21.1", "fabric-26.3", "neoforge-26.3"):
                    (root / f"villagermarket-{target}-0.0.2-alpha.jar").touch()
                trace = root / "uploads.txt"
                env = dict(os.environ, MODRINTH_TOKEN="test", CURSEFORGE_TOKEN="test",
                           MODRINTH_PROJECT_ID="test", CURSEFORGE_PROJECT_ID="123",
                           TAG="0.0.2-alpha", RELEASE_TYPE="alpha", TEST_DIR=root.as_posix(),
                           TEST_PYTHON=Path(sys.executable).as_posix())
                # Mock network and uploads; keep the actual publisher orchestration
                # and Python category resolver. No credentials or services are used.
                harness = r'''
source "$1" check-env
python3() { "$TEST_PYTHON" "$@"; }
http_code() {
  case "${!#}" in
    */game/versions) cp "$TEST_DIR/versions.json" "$1"; printf 200 ;;
    */game/version-types) cp "$TEST_DIR/types.json" "$1"; printf 200 ;;
    */files) printf '{}' > "$1"; printf 403 ;;
    *) echo "Unexpected network request" >&2; exit 1 ;;
  esac
}
modrinth_upload() { echo "Modrinth $1" >> "$TEST_DIR/uploads.txt"; }
curseforge_upload() { echo "CurseForge $4" >> "$TEST_DIR/uploads.txt"; }
github_release() { echo "GitHub $1" >> "$TEST_DIR/uploads.txt"; }
upload 0.0.2-alpha "$TEST_DIR/notes.md" "$TEST_DIR"
'''
                result = subprocess.run(
                    [os.environ.get("BASH_EXECUTABLE", "bash"), "-c", harness, "test",
                     Path(__file__).with_name("release-publish.sh").resolve().as_posix()],
                    env=env, capture_output=True, text=True,
                )
                if valid:
                    self.assertEqual(result.returncode, 0, result.stderr)
                    uploads = trace.read_text().splitlines()
                    self.assertEqual(len(uploads), 7)
                    self.assertEqual(uploads[3:6], [
                        "CurseForge [11779, 10150, 99901, 99902, 11135]",
                        "CurseForge [17045, 7499, 99901, 99902, 14454]",
                        "CurseForge [17045, 10150, 99901, 99902, 14454]",
                    ])
                else:
                    self.assertNotEqual(result.returncode, 0)
                    self.assertIn("minecraft-26-3", result.stderr)
                    self.assertFalse(trace.exists(), "No upload should run when a required target is missing")


if __name__ == "__main__":
    unittest.main()
