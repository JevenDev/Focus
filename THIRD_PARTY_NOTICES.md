# Licensing and third-party notices

Focus is Copyright (c) 2026 jvn and All Rights Reserved by default.

## MIT-licensed shoulder-surfing camera component

The following source files are licensed under the MIT License in
[`third_party/ShoulderSurfing-MIT.txt`](third_party/ShoulderSurfing-MIT.txt),
rather than Focus's default proprietary license:

- `src/main/java/com/jvn/focus/client/camera/FocusShoulderSurfingCameraSystem.java`
- `src/main/java/com/jvn/focus/client/camera/FocusCameraBasisUtil.java`

Each listed file also carries an `SPDX-License-Identifier: MIT` header. The
exception is intentionally file-scoped; surrounding lock-on behavior, camera
policies, mixins, compatibility, configuration, crosshair, player-visibility,
targeting, UI, assets, and integration code remain All Rights Reserved unless
another file explicitly states otherwise.

## Shoulder Surfing Reloaded attribution

The collision-aware dynamic camera offsets, camera zoom collision probing,
and camera-distance interpolation were adapted from
[Shoulder Surfing Reloaded](https://github.com/Exopandora/ShoulderSurfing) by
Exopandora, itself a port of Sabar's original Shoulder Surfing mod. The
upstream copyright and permission notice are retained in
`third_party/ShoulderSurfing-MIT.txt`.
