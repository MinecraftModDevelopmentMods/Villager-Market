# Changelog

Each pull request adds its own bullets at the top of **Unreleased**, written for players. When a merge lands, the Discord message shows only the bullets that pull request added.

Push a version tag to publish. Alpha and beta releases use only **Unreleased**. Stable releases include those new notes under the tagged version, followed by every alpha/beta section since the previous stable release, newest first. The same notes go to CurseForge, Modrinth, and GitHub.

After publishing, the action prepares the next development version and archives the new notes under the released heading. It runs the full CI suite on a temporary branch before committing the update directly to **main**, without a follow-up PR. Earlier sections stay in place without duplication. A stable promotion may have no new notes; its empty heading still marks the next stable cutoff. Concurrent main changes are never overwritten, and unpublished notes changed since the tag are preserved for manual reconciliation.

## Unreleased

- A Market Automaton trades on its own, at the same speed as a hopper. Choose an offer, put the payment in the two slots or let a hopper fill them, and take the result from the bottom. The choice stays when the trade locks, and you can take the payment back out of the window.
- The furnace arrow sits between the payment and the result. A button on the right side of the window chooses whether the block ignores redstone, runs only while powered, or runs only while unpowered.
- Each automaton offer shows `[max: uses left]`, or `[locked]` once none remain. That label updates while the window stays open.
- Similar is the icon beside the redstone button. It also buys other villagers' copies of the selected trade and picks the cheapest one the slots can pay.

## 2.0.0-alpha

- Under the trades, [available / max until lock] explains the two numbers on each offer.
- Selecting an offer shows its inputs beside the buy buttons, each with how many of that item you are carrying. Those slots cannot be clicked.
- Buying updates that carried count, and how many times you can still pay.
- Experience from a purchase appears on you. It no longer drops on the villager.
- The trade list shows a scrollbar when there are more offers than fit. Drag it, or use the mouse wheel.
- The Villager Market recipe appears in the recipe book once you have planks or an emerald block.
- The Villager Market is a barrel. Each side has a villager face standing one pixel out, with the nose one pixel farther, and an emerald in the middle of the top and the bottom.
- The market window uses the vanilla inventory frame, buttons, and trade arrows.
- Place a Villager Market (planks around an emerald block) and trade with every villager within 48 blocks, 16 up and 16 down. Offers are grouped by the villager's name, or by profession. Show All lists offers you cannot pay for. Turn it off and the button reads Available, listing only offers you can pay. x1 buys once, and xN buys as many as you can pay for and the offer still has left.

## 1.0.2

- fixed #12 concurrent list CTD

## 1.0.1

- update item highlight
- fixed issues around server/client network handling

## 1.0.0

- moved to MMD servers
- jar should now be signed
