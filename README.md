<div align="center">

  <h2><strong><em>DO NOT</em> USE THIS ON PUBLIC SERVERS WITHOUT ADMIN PERMISSION, YOU ARE VERY LIKELY TO BE BANNED.</strong></h2>
  
</div>

<div align="center">
  
<a href="https://modrinth.com/mod/focus/settings/versions?l=neoforge"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/cozy/supported/neoforge_64h.png" alt="Available for NeoForge"></a>
<a href="https://modrinth.com/mod/focus/settings/versions?l=forge"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/cozy/supported/forge_64h.png" alt="Available for Forge"></a><br>
<a href="https://modrinth.com/mod/focus" target="_blank" rel="noopener noreferrer"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/compact-minimal/available/modrinth_46h.png" alt="Available on Modrinth"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/focus" target="_blank" rel="noopener noreferrer"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/compact-minimal/available/curseforge_46h.png" alt="Available on CurseForge"></a>
<a href="https://github.com/JevenDev/Focus" target="_blank" rel="noopener noreferrer"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/compact-minimal/available/github_46h.png" alt="Available on GitHub"></a>
  
</div>

![focus banner text, and a player using the z-targeting on a blaze](https://cdn.modrinth.com/data/cached_images/43e93503fef9ce0dd15a5c49e09216710b783a06.png)

Focus adds a proper **combat lock-on system** for both **first- and third-person play**, with targeting and camera behavior inspired by *Ocarina of Time* and *Elden Ring*.

- **Lock onto nearby enemies** and keep your camera focused on the fight
- **Swap between targets** with deliberate mouse or right-stick flicks
- **Swap shoulders on the fly** depending on how you want combat framed
- **Save your own camera presets** instead of being stuck with one setup
- Use a full **camera editor** to tweak:
  - offsets
  - distance
  - rotation
  - shoulder behavior

![Gameplay footage 1: showcasing z-targeting indoors, player fighting skeletons from a spawner](https://i.imgur.com/eZVekT9.gif)

![features](https://cdn.modrinth.com/data/cached_images/ec0e4dc78ec1a652eb11b233dd2926f7461fe770.png)

Focus also helps fix one of the big annoyances with third-person combat by supporting a **corrected crosshair**, so aiming and hit feedback feel much more consistent while locked on.

On top of that, you can also:

- use **target filters**
- choose between different **lock-on indicator styles**
- enable **cinematic bars**
- make your player **fade out** when they get in the way of the camera

## Server admin policy

When Focus is installed server-side, admins can use the generated `focus-server.toml` config to force or disable selected client features for players using Focus.

The server can control lock-on availability, first-person/front third-person while locked on, target filtering, player targeting through filters, and corrected crosshair/hit-ray behavior. To disable locking onto players, set `targetFiltersEnabled=FORCE_ON`, `targetFilterMode=EXCLUDE`, and `filterPlayers=FORCE_ON`.

![Gameplay footage 2: player switching between two targets, the spider and the skeleton (spider jockey), using mouse direction flicks](https://i.imgur.com/wjYIE2b.gif)

![keybinds](https://cdn.modrinth.com/data/cached_images/201d5ce49ba16974e3c3b0b562c392e03f38e35f.png)

## Default keybinds

- **Lock On Target** - `V`
  - Locks onto the best nearby target
  - Press again to disengage

- **Previous Target / Next Target** - *Unassigned by default*
  - Enable **Manual Target Switching** in Focus's Targeting settings
  - Assign either or both bindings in Minecraft's Controls menu
  - Mouse and right-stick target flicks remain the default switching mode


- **Swap Shoulder** - `X`
  - Switches the camera between left and right shoulder


- **Open Camera Editor** - `F6`
  - Opens the in-game camera editor
  - Lets you preview and adjust your current camera setup live


- **Open Indicator Creator** - `F7`
  - Paint a custom 16×16 lock-on icon with an HSV color wheel
  - Preview and adjust its size, position, and animation


- **Adjust Camera Offsets** *(same default controls as Shoulder Surfing Reloaded)*  
  These can be used while locked on, or while previewing in the camera editor:

  - **Move camera up** - `Page Up`
  - **Move camera down** - `Page Down`
  - **Move camera in** - `Up Arrow`
  - **Move camera out** - `Down Arrow`
  - **Move camera left** - `Left Arrow`
  - **Move camera right** - `Right Arrow`

![Gameplay footage 3: player swapping between shoulders while targeting](https://i.imgur.com/Kx3ewj5.gif)

![compatibility](https://cdn.modrinth.com/data/cached_images/1252c11050b7daf8b8621712b58dd1005e7ba982.png)

Focus currently has **native compatibility** with:

- **[Shoulder Surfing Reloaded](https://modrinth.com/mod/shoulder-surfing-reloaded)**
- **[Controlify](https://modrinth.com/mod/controlify)**
- **[MidnightControls](https://modrinth.com/mod/midnightcontrols)**

These integrations are optional dependencies.
When locked on, Focus temporarily owns the camera so Shoulder Surfing's Decoupled Camera state cannot fight the lock-on movement and body rotation.
With Controlify installed, Focus registers controller bindings for lock-on controls and uses Controlify's look-input API for right-stick target swapping while Focus owns the camera.
With MidnightControls installed, Focus uses MidnightControls' automatic modded keybind support for lock-on controls and a native camera compatibility hook for right-stick target swapping while Focus owns the camera.

If there's a specific mod you would like compatibility with, open an issue in the GitHub repo.

<div align="center">
  <p><strong><em>Note: Planned integrations have NOT been tested yet.<br>Please don’t report issues to those developers. Report any bugs to the <a href="https://github.com/JevenDev/Focus/issues">GitHub</a> or via Discord DM (ijvn).</em></strong></p>
</div>

![roadmap](https://cdn.modrinth.com/data/cached_images/04825ea0e2e5462ffa075e783ca38b0c63a36d34.png)

## Version and Loaders

- ✅ **NeoForge 1.21.1** [Active development]
- ⛔ **NeoForge 1.20.1** [Not planned]
- ⛔ **Forge 1.21.1** [Not planned]
- ✅ **Forge 1.20.1** [Active development]
- 🚧 **Fabric 1.21.1** [Planned port]
- 🚧 **Fabric 1.20.1** [Planned port]

## Planned Features

- Additional controller polish and default binding presets
- Overhaul of the entire camera editor screen
- ✅ Custom indicator icon creator with colour, size, target-relative position, bundled or resource-pack textures, and static/orbit animation
- Server-side admin commands for live policy editing

![credits & license](https://cdn.modrinth.com/data/cached_images/5fd3ad80e342e6985dd6ebda1f7afd9c48749fce.png)

## Credits

The camera system in Focus derives from **[Exopandora's](https://modrinth.com/user/Exopandora)** port of **[Sabar's](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1287308-shoulder-surfing-modded-third-person-camera)** mod, **[Shoulder Surfing Reloaded](https://modrinth.com/mod/shoulder-surfing-reloaded)**, with further modifications made for Focus.

## License

Focus is All Rights Reserved by default. Only the shoulder-surfing camera files explicitly marked `SPDX-License-Identifier: MIT` use the [MIT license text](third_party/ShoulderSurfing-MIT.txt). See [LICENSE.txt](LICENSE.txt) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the exact boundary and Shoulder Surfing Reloaded attribution.

Feel free to use this mod in modpacks, videos, etc. Just provide a link back to this page if possible :)

Looking to port the mod to your favourite loader/version outside of my scope? Feel free to, and let me know so I can add a sub-section to direct users to it!

For any general queries/unlisted questions, DM me on Twitter (@prodbyjvn) / Discord (ijvn).

<div align="center">
  
  <p><strong>⚠ <em>This mod ONLY exists on Modrinth & CurseForge as of April 2026. Any sites hosting this mod outside of Modrinth/CurseForge are not official releases.</em> ⚠</strong></p>
  
</div>
