# Changelog

Each pull request adds its own bullets at the top of **Unreleased**, written for players. When a merge lands, the Discord message shows only the bullets that pull request added.

Push a version tag to publish. Alpha and beta releases use only **Unreleased**. Stable releases include those new notes under the tagged version, followed by every alpha/beta section since the previous stable release, newest first. The same notes go to CurseForge, Modrinth, and GitHub.

After publishing, the action prepares the next development version and archives the new notes under the released heading. It runs the full CI suite on a temporary branch before committing the update directly to **main**, without a follow-up PR. Earlier sections stay in place without duplication. A stable promotion may have no new notes; its empty heading still marks the next stable cutoff. Concurrent main changes are never overwritten, and unpublished notes changed since the tag are preserved for manual reconciliation.

## Unreleased

- Place a Villager Market (planks around an emerald block) and trade with every villager within 48 blocks, 16 up and 16 down. Offers are grouped by the villager's name, or by profession. Show All lists offers you cannot pay for. x1 buys once, and xN buys as many as you can pay for and the offer still has left.

## 1.0.2

- fixed #12 concurrent list CTD

## 1.0.1

- update item highlight
- fixed issues around server/client network handling

## 1.0.0

- moved to MMD servers
- jar should now be signed
