
# Pi's Cosmetics Extension

Framework
=======

This mod extends the [Accessories mod](https://github.com/wisp-forest/accessories?tab=readme-ov-file) and
[Mega Showdown mod](https://github.com/yajatkaul/CobblemonMegaShowdown) so that items in all slots
can be rendered physically on the player model. Rendering is done through a custom
pipeline that uses [Geckolib](https://github.com/bernie-g/geckolib), allowing for animated attachments
and features of textures like transparency and emission.

The mod only contains generic items which can be cloned and overidden in many aspects with datapack JSONs
and an accompanying resourcepack.
This allows users to add their own custom cosmetics that are worn in any slot provided by Accessories and
Mega Showdown.

Making new items:
============

New items are effectively copies of existing generic items with NBT data. To override the appearance of
generic items, they must be specified in a datapack, structured as `data/your_namespace/cosmetics/newitem.json`.

```
📁 your_datapack/
├── pack.mcmeta                      
├── pack.png                         
└── 📁 data/
    └── 📁 piscosmetics/             
        └── 📁 cosmetic/                  
            ├── my_cool_hat.json
            ├── special_sword.json
            └── ...
```

The contents of the JSON file determine the various properties of the NBT tagged item. All fields are optional
and will fall back onto default values if left empty. Note that translational offsets for items are additive to
defaults, but scaling values directly override their defaults.

Each item can be applied with a particle effect that exists in the registry. This is limited to the default
Minecraft particles, as well as particles added by other mods. The position and properties of attached simple
particles can be modified.

**Example:**

```json
{
    "slot": "hat",
    "model": "your_namespace:geo/item/custom_model",
    "texture": "your_namespace:textures/item/custom_texture",
    "animation": "your_namespace:custom_animation",
    "name": "My Custom Hat",
    "translate_x": 0.0,
    "translate_y": 0.0,
    "translate_z": 0.0,
    "rotate_x": 0.0,
    "rotate_y": 0.0,
    "rotate_z": 0.0,
    "scale": 1.0,
    "particles": {
        "type": "minecraft:flame",
        "rate": 5,
        "spread": 0.5,
        "offset_x": 0.0,
        "offset_y": 0.0,
        "offset_z": 0.0
    }
}
```

You also need to provide assets with which to override the generic item in a resourcepack. These assets should be under a custom namespace within the resourcepack, and referenced accordingly in the JSON. Ensure file format and directory structure is consistent
with examples. In the case of animations, only the name of the animation before `.animation.json` is required; folder and and suffix will be appended automatically.

**Example:**

```
📁 your_resourcepack/
└── 📁 assets/
    └── 📁 piscosmetics/
        ├── 📁 geo/
        │   └── 📁 item/
        │       └── my_cool_hat.geo.json
        │
        ├── 📁 textures/
        │   └── 📁 item/
        │       └── my_cool_hat.png
        │
        └── 📁 animations/
            └── 📁 item/
                └── my_cool_hat.animation.json
```

By default, the renderer handles transparent/translucent textures.

For emissive textures, simply add another texture file suffixed with `_emissive.png`.
The mod will automatically match that to an identically named texture and apply the glow
based on the emissive file's transparency.


### Models

Models used must be in the `.geo.json` format. It is recommended that you model or convert 
an existing model in Blockbench to a Bedrock Entity with per-face UVs. Ensure the format version is `1.12.0`.
The name of the root bone in the model specifies the bone on the player model it will attach
to, from the following list:

- `armorHead`
- `armorBody`
- `armorleftArm`
- `armorleftLeg`
- `armorrightArm`
- `armorrightLeg`

It is generally good practice to have the origin of this bone be `[0, 0, 0]` for easy testing
of the correct offset values.

**Example:**

```json
{
    "format_version": "1.12.0",
    "minecraft:geometry": [
        {
            "description": {
                "identifier": "geometry.test_cube",
                "texture_width": 16,
                "texture_height": 16
            },
            "bones": [
                {
                    "name": "leftArm",
                    "pivot": [0, 0, 0],
                    "cubes": [
                        {
                            "origin": [-8, -8, 0],
                            "size": [16, 16, 1],
                            "uv": [0, 0]
                        }
                    ]
                }
            ]
        }
    ]
}
```

Textures are generally 16x or 32x, but can be any size theoretically as per-face UVs support them.

Animations are detected and applied in a similar manner as emissive textures.
