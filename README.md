
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

Datapack:
============

New items are effectively copies of existing generic items with NBT data. To override the appearance of
generic items, they must be specified in a datapack, structured as `data/your_namespace/cosmetics/newitem.json`.

```
📁 your_datapack/
├── pack.mcmeta                      
├── pack.png                         
└── 📁 data/
    └── 📁 your_namespace/             
        └── 📁 cosmetic/                  
            ├── my_cool_hat.json
            ├── special_sword.json
            └── ...
```

The contents of the JSON file determine the various properties of the NBT tagged item. All fields are optional
and will fall back onto default values if left empty.
The following fields are available:

```json
{
    "slot": "feet",
    "name": "Dragon Boots",
    "model": "your_namespace:cosmetic_feet",
    "texture": "your_namespace:cosmetic_feet",
    "animation": "your_namespace:cosmetic_feet",
    "attach": "right_leg",
    "translate_x": 0.1,
    "translate_y": 0.2,
    "translate_z": -0.1,
    "rotate_x": 5.0,
    "rotate_y": 10.0,
    "rotate_z": 0.0,
    "scale": 0.9,
    "hide_armor": ["feet", "elytra"],
    "icon": "your_namespace:item/cosmetic_feet",
    "particle_emitters": [
        {
            "trigger": "walking",
            "particle": "minecraft:end_rod",
            "rate": 5,
            "spread": 0.3,
            "count": 2,
            "offset_y": 0.1
        },
        {
            "trigger": "sprinting",
            "particle": "your_namespace:cosmetic_particle",
            "rate": 10,
            "spread": 0.5,
            "count": 1,
            "offset_y": 0.5,
            "custom_size": 0.5,
            "lifetime": 20,
            "texture": "your_namespace:lettera"
        },
        {
            "trigger": "jumping",
            "particle": "your_namespace:entity_effect",
            "rate": 3,
            "spread": 1.0,
            "count": 5,
            "offset_y": 0.0,
            "color": "#FF5500"
        }
    ]
}
```
- `"slot"`: Specifies which inventory slot the cosmetic goes in; this can be either something from the Accessories AI, 
one of the Mega Showdown slots, or a vanilla armour slot. The available slots are:
    
  - Accessories slots
    - `"hat"`
    - `"back"`
    - `"belt"`
    - `"cape"`
    - `"charm"`
    - `"face"`
    - `"hand"`
    - `"necklace"`
    - `"ring"`
    - `"shoes"`
    - `"wrist"`
    - `"anklet"`

  - Mega Showdown slots
    - `"dynamax_slot"`
    - `"mega_slot"`
    - `"tera_slot"`
    - `"z-slot"`

  - Vanilla slots
    - `"head"`
    - `"chest"`
    - `"legs"`
    - `"feet"`


- `"name"`: Display name for item in your inventory


- `"model"`, `"texture"`, `"animation"`: The namespace and file name of your `.geo.json` model file, texture file, and
animation file. See the resourcepack section of this guide for information on how to format and place these files in the resourcepack.


- `"attach"`: which player bone it attaches to and moves with in all normal Minecraft body animations. Note that paired rendered slots, which are `"shoes"`, `"legs"` and `"feet"`
cannot be overridden this way. Valid player bones are:
    - `"head"`
    - `"body"` or `"chest"`
    - `"left_arm"`
    - `"right_arm"`
    - `"left_leg"`
    - `"right_leg"`


- Offsets:
  - `"translate_x"`: Shifts cosmetic left (+) or right (-).
  - `"translate_y"`: Shifts cosmetic up (+) or down (-).
  - `"translate_z"`: Shifts cosmetic forwards (+) or backwards (-).
  - `"rotate_x"`: Tilts cosmetic forwards (+) or backwards (-).
  - `"rotate_y"`: Turns cosmetic left (+) or right (-).
  - `"rotate_z"`: Rolls cosmetic left (+) or right (-).
  - `"scale"`: Scales the cosmetic up or down in all dimensions. Note that this is an override value and is not added
    to any hidden defaults, unlike other offset values.


- `"hide_armor"`: Specifies vanilla armour slot to not be rendered, in case it clips into your cosmetic. Can be a single
slot or a list like `["chest", "legs"]`. `"elytra"` is a special value that specifically only disables the elytra rendering;
similarly, `"chest"` does not affect the elytra.


- `"icon"`: Overrides the in-hand and the dropped texture of the item with a `.png` file in the resourcepack. Not
including this field results in hand and ground renderer being the model of the cosmetic itself.


- `"particle_emitters"`: This is a complicated field that allows you to place as many particle emitters as you like (in a list) anchored
to the cosmetic. Each emitter can be fully customised, and supports custom particles derived from `.png` files you place
in the resourcepack. Emitters have the following properties:
  - `"trigger"`: Specifies when the emitter is active and producing particles. Available values are:
    - `"always"` is always active, default
    - `"walking"` is active when you are moving
    - `"sprinting"` is active when you are moving past a certain speed
    - `"jumping"` is active when you are moving upwards in air
    - `"falling"` is active when you are moving downwards in air
    - `"gliding"` is active during elytra flight
    - `"sneaking"` is active when crouching
    - `"swimming"` is active when you are in water
    - `"on_ground"` is active when you are touching the ground
    - `"in_air"` is active when you are not touching the ground
    - `"hurt"` is active when you take damage
    - `"on_fire"` is active when you are burning

  - `"particle"`: Specifies the type of particle produced by the emitter. This can be any vanilla minecraft particle, any
  registered particles from other installed mods, or for fully custom particles in the resourcepack, use `piscosmetsics:cosmetic_particle`.
  Note that all custom particles are not affected by physics.
  - `"rate"`: Number of ticks between particle spawns.
  - `"spread"`: How much the particles spread out.
  - `"count"`: How many particles are produced per rate.
  - `"offset_x"`, `"offset_y"`, and `"offset_z"` translate the emitters in the same direction as their equivalent cosmetic fields.
  - `"lifetime"`: How long the particle lasts, in ticks.
  - `"color"`: What colour the particle is; only works with `"particle": "minecraft:entity_effect"`.
  - `"texture"`: Path to the custom particle `.png` in the resourcepack. Only use this with `"particle": "piscosmetsics:cosmetic_particle"`.


### Note
When specifying resource locations, always only use `your_namespace:filename` without any folder or suffixes.

# Resourcepack

Your resourcepack should be structured as follows:

```
📁 your_resourcepack/
└── 📁 assets/
    └── 📁 your_namespace/
        ├── 📁 geo/
        │   └── 📁 item/
        │       └── my_cool_hat.geo.json
        │
        ├── 📁 textures/
        │   └── 📁 item/
        │       └── my_cool_hat.png
        │
        └── 📁 animations/
            └── my_cool_hat.animation.json
```

By default, the renderer handles transparent/translucent textures.

For emissive textures, simply add another texture file suffixed with `_emissive.png`.
The mod will automatically match that to an identically named texture and apply the glow
based on the emissive file's transparency.


## Models

Models used must be in the `.geo.json` format. It is recommended that you model or convert 
an existing model in Blockbench to a Bedrock Entity with per-face UVs, with a single bone on the topmost level.

It is generally good practice to have the origin of the topmost bone be `[0, 0, 0]` for easy testing
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
                    "name": "bone",
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

## Dynamic Animations

The renderer is capable of applying directional animation switches in accordance with the player model.
The idea is to simulate the swaying of soft bodies when the direction of the wearing changes
This is set up within the animation file itself, by creating animations named as follows:

 - `idle`
 - `walk_forward`
 - `walk_backward`
 - `walk_left`
 - `walk_right`
 - `jump`
 - `fall`
 - `glide`

The glide animation switch can be used in tandem with elytra hiding and particle generation trigger while gliding to create
animated wings with trailing effects.

# Acknowledgements
Thank you to:

Noowtie for sending me down this rabbit hole in the first place.

Puremask for providing very enlightening code samples, especially for serverside particle rendering.