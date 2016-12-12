Hi there! This is the source code for my [GDL December Jam](https://itch.io/jam/game-dev-league-december-jam) entry, [Oobleck](https://ske.itch.io/oobleck).

I only had a week to make this game, a lot of which was spent on the damn goo shader. Because of that, the code is... sub-optimal.

For example:
* I didn't bother to make a screen/scene system, so the menu and the credits are level -1 and 8, respectively. An if() in the main render() method will render the respective pages instead.
* There's a completely empty class named "Boss" - I thought I would have multiple bosses and thus needed a place to extract common functionality to.
* "GooGame.java" is extremely crowded and fulfills far too many tasks - entity rendering, menus, camera movement, level loading, transitions, audio mixing, debug keys, and more.
* Levels are specified as plain ASCII grids in a .txt file.
* All the boss' behavior is in a single method with an if/else chain on the current boss state. It reuses a single timer field multiple times, and doesn't try to reset the state upon transition (it mostly kinda works though :D)
* Assets are a bunch of static fields with no management.
* Nothing gets disposed. Ever.
* There's no central event bus, so side effects, audio calls, etc are littered everywhere.
* Player movement code is one huge tangle of ifs, flags and timers. I'm surprised it works.
* The goo effect itself consists of several blur passes, plus a complex coloring/threshold shader. It runs like arse on older/integrated systems.

All this and more available for $0.00 on the above source code repo!

License: CC-Attrib or something