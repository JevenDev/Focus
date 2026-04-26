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

Focus adds a proper **lock-on system** for **third-person combat**, with a camera that stays centered on your target and makes fights feel a lot more deliberate.

- **Lock onto nearby enemies** and keep your camera focused on the fight
- **Swap between targets** with directional mouse flicks
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

![Gameplay footage 2: player switching between two targets, the spider and the skeleton (spider jockey), using mouse direction flicks](https://i.imgur.com/wjYIE2b.gif)

![keybinds](https://cdn.modrinth.com/data/cached_images/201d5ce49ba16974e3c3b0b562c392e03f38e35f.png)

## Default keybinds

- **Lock On Target** - `V`
  - Locks onto the best nearby target
  - Press again to disengage


- **Swap Shoulder** - `X`
  - Switches the camera between left and right shoulder


- **Open Camera Editor** - `F6`
  - Opens the in-game camera editor
  - Lets you preview and adjust your current camera setup live


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

Focus currently has **native compatibility** with **[Shoulder Surfing Reloaded](https://modrinth.com/mod/shoulder-surfing-reloaded)** as an **optional dependency**.

`Disable "Decoupled camera" in the 'Camera' section of the client config for **SHOULDER SURFING**. As of [1.0.0-beta.2], the decoupled camera doesn't play well with focus. I'm working on proper integration for this :P`

Support for other mods is also planned, including:

- **[Controlify](https://modrinth.com/mod/controlify)**
- **[MidnightControls](https://modrinth.com/mod/midnightcontrols)**

If there's a specific mod you would like compatibility with, open an issue in the GitHub repo.

<div align="center">
  <p><strong><em>Note: These mods have NOT been tested yet.<br>Please don’t report issues to those developers. Report any bugs to the <a href="https://github.com/JevenDev/Focus/issues">GitHub</a> or via Discord DM (ijvn).</em></strong></p>
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

- Controller support through Controlify & MidnightControls 
- Overhaul of the entire camera editor screen
- Custom indicator icon creator; colour, size, position, texture, etc.
- Server-side admin control commands/config to prevent certain features for users

![credits & license](https://cdn.modrinth.com/data/cached_images/5fd3ad80e342e6985dd6ebda1f7afd9c48749fce.png)

## Credits

The camera system in Focus derives from **[Exopandora's](https://modrinth.com/user/Exopandora)** port of **[Sabar's](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1287308-shoulder-surfing-modded-third-person-camera)** mod, **[Shoulder Surfing Reloaded](https://modrinth.com/mod/shoulder-surfing-reloaded)**, with further modifications made for Focus.

## License

This project is licensed under the **[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)**.

Feel free to use this mod in modpacks, videos, etc. Just provide a link back to this page if possible :)

Looking to port the mod to your favourite loader/version outside of my scope? Feel free to, and let me know so I can add a sub-section to direct users to it!

For any general queries/unlisted questions, DM me on Twitter (@prodbyjvn) / Discord (ijvn).

<div align="center">
  
  <p><strong>⚠ <em>This mod ONLY exists on Modrinth & CurseForge as of April 2026. Any sites hosting this mod outside of Modrinth/CurseForge are not official releases.</em> ⚠</strong></p>
  
</div>
