# Boss bonuses

## Matching-boss progress

Confirmed personal NPC or event loot awards bonus hatch progress equal to the active matching encounter time. It is additional to the normal clock: a two-minute fight can add two bonus minutes, while an eight-minute active Barrows run can add eight. Boss HP and raw kill counts do not set the bonus. Barrows includes active movement in the surface/crypt regions until the chest is looted. Other encounters count matching combat; shared eggs accept any of their bosses/phases. Moons combat time is accumulated across its three fights until the Lunar Chest reward.

Bonus time only fills the egg currently assigned. It cannot overflow into an unrelated next egg. Death, logout, world hopping, reset or changing eggs clears unclaimed encounter time. Duplicate raw-NPC/Loot Tracker callbacks are filtered. EggScape listens to Loot Tracker events without declaring a service dependency on it. Keep Loot Tracker enabled for chest/event rewards; NPC rewards also have direct native-loot event support. Existing remote/history records are not imported as new kills.
