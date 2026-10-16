
# Pi's Cosmetics Extension

Framework
=======

This mod renders cosmetics physically on the player model, on Fabric and NeoForge for Minecraft 1.21.1. It works on
its own (vanilla armour slots), and also supports the [Accessories mod](https://github.com/wisp-forest/accessories?tab=readme-ov-file),
[Mega Showdown mod](https://github.com/yajatkaul/CobblemonMegaShowdown) and Genesis Forms when they are installed, so that
items in all their slots can be rendered too. All three are optional; only GeckoLib (and Fabric API on Fabric) is required.
Rendering is done through a custom pipeline that uses [Geckolib](https://github.com/bernie-g/geckolib), allowing for animated
attachments and features of textures like transparency and emission.

The mod only contains generic items which can be cloned and overidden in many aspects with datapack JSONs
and an accompanying resourcepack.
This allows users to add their own custom cosmetics that are worn in any slot provided by Accessories and
Mega Showdown, and to give the key items of Mega Showdown and Genesis Forms their own looks.

Datapack
============

New items are effectively copies of existing generic items with NBT data. To override the appearance of
generic items, they must be specified in a datapack, structured as `data/your_namespace/cosmetic/newitem.json`.

Cosmetics use the universal cosmetic item, which goes in whatever slot the JSON's `"slot"` field says:

```
/give @s piscosmetics:cosmetic[custom_data={cosmetic:"your_namespace:my_cool_hat"}]
```

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

The contents of the JSON file determine the various properties of the NBT tagged item. All fields except `"slot"` are
optional and will fall back onto default values if left empty.
The following fields are available:

```json
{
    "slot": "feet",
    "name": "Dragon Boots",
    "color": "gold",
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
    "copyable": true,
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
            "particle": "piscosmetics:cosmetic_particle",
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
            "particle": "minecraft:entity_effect",
            "rate": 3,
            "spread": 1.0,
            "count": 5,
            "offset_y": 0.0,
            "color": "#FF5500"
        }
    ]
}
```
- `"slot"`: Specifies which inventory slot the cosmetic goes in; this can be either something from the Accessories API, 
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
    - `"z_slot"`

  - Vanilla slots
    - `"head"`
    - `"chest"`
    - `"legs"`
    - `"feet"`

  Accessories and Mega Showdown slots need those mods installed; the vanilla slots always work, and can also be
  equipped by right-clicking like armour. A slot that doesn't exist on a server simply can't be equipped there. To use
  a slot added by another mod or datapack, also add `piscosmetics:cosmetic` to that slot's item tag,
  `data/accessories/tags/item/<slot>.json`.


- `"name"`: Display name for item in your inventory


- `"color"`: Colour of the name everywhere it shows, as a colour name like `"gold"` or a hex code like `"#FF8800"`.
Names are aqua by default.


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


- `"copyable"`: Set to `false` so this cosmetic's blueprints can't be copied. See the blueprints section.


- `"key_items"` and `"render_for"`: Used for key item looks. See the key items section.


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
  registered particles from other installed mods, or for fully custom particles in the resourcepack, use `piscosmetics:cosmetic_particle`.
  Note that all custom particles are not affected by physics.
  - `"rate"`: Number of ticks between particle spawns.
  - `"spread"`: How much the particles spread out.
  - `"count"`: How many particles are produced per rate.
  - `"offset_x"`, `"offset_y"`, and `"offset_z"` translate the emitters in the same direction as their equivalent cosmetic fields.
  - `"custom_size"`: Size of a custom particle.
  - `"lifetime"`: How long the particle lasts, in ticks.
  - `"color"`: What colour the particle is; only works with `"particle": "minecraft:entity_effect"`.
  - `"texture"`: Path to the custom particle `.png` in the resourcepack. Only use this with `"particle": "piscosmetics:cosmetic_particle"`.


### Note
When specifying resource locations, always only use `your_namespace:filename` without any folder or suffixes.

# Key Items

The key items of Mega Showdown and Genesis Forms (Key Stones, Mega Bracelets, Z-Rings, Dynamax Bands, Tera Orbs and so on)
can be drawn as cosmetics while worn. The item keeps working exactly as before. With Genesis Forms installed, its key items
can be worn in these Accessories slots: wrist (mega_bracelet, mega_cuff, dynamax_band), ring (mega_ring, z_ring, z_power_ring,
key_stone), necklace (mega_charm) and belt (tera_orb).

There are two ways to give a key item a look:

- `"render_for"`: A list of items this cosmetic is the default look for, e.g. `["genesisforms:key_stone"]`. Every one of
those items worn in an Accessories slot shows this cosmetic, including its `hide_armor` and `particle_emitters`. `"slot"` then
only picks the default position.
- Per item: a single item can be given its own look, which wins over `"render_for"`. Players do this with blueprints, or you
can use a command:

```
/give @s genesisforms:key_stone[custom_data={cosmetic:"your_namespace:golden_key_stone"}]
```

An item with its own look shows it in the tooltip (`Look: ...`). To take the look off, put the item alone in a grindstone.
It comes out plain, and nothing is given back.

# Blueprints

A blueprint carries one cosmetic and is used up in a smithing table. Blank blueprints are crafted from a paper and a lapis
lazuli. Every cosmetic's blueprint is in the "Pi's Cosmetics" creative tab, or can be given with:

```
/give @s piscosmetics:cosmetic_blueprint[custom_data={blueprint:"your_namespace:my_cool_hat"}]
```

In the smithing table, put the blueprint in the left slot and leave the right slot empty. What goes in the middle decides
what comes out:

- A diamond: the wearable cosmetic.
- A key item: that key item with the look. Only works for cosmetics with `"key_items"`, which can't be made from a diamond.
- A blank blueprint: two copies of the blueprint. Doesn't work for uncopyable blueprints.

Two cosmetic fields control blueprints:

- `"key_items"`: Turns the cosmetic into a key item look and picks which key items its blueprint goes on, any of `"mega"`,
`"z_move"`, `"dynamax"` and `"tera"`. Can be a single type or a list like `["mega", "tera"]`. Any other value is an error,
and the cosmetic won't load.
- `"copyable": false` stops all of a cosmetic's blueprints from being copied. For a single blueprint, add `copyable:false`
to its custom data next to `blueprint`.

### For server owners
All of these can be changed in a datapack:

- `data/piscosmetics/tags/item/blueprint_base.json`: what wearable cosmetics are made from, a diamond by default.
- `data/piscosmetics/recipe/blank_blueprint.json`: the recipe for blank blueprints.
- `data/piscosmetics/tags/item/key_items/<type>.json`: which items count as `mega`, `z_move`, `dynamax` and `tera` key items.
Add your own Genesis Forms key items here.

Blueprints can also be handed out through loot tables with vanilla's `set_custom_data` function.

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
        │   └── 📁 particle/
        │       └── my_particle.png
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

Todredmi for challenging me to evolve this for Fabric.

Noowtie for sending me down this rabbit hole in the first place.

Puremask for providing very enlightening code samples, especially for serverside particle rendering.
