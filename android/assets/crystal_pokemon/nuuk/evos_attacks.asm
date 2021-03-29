TaillowEvosAttacks:
	db EVOLVE_LEVEL, 22, SWELLOW
	db 0 ; no more evolutions
	db 1, PURSUIT
	db 4, GROWL
	db 7, PECK
	db 10, TWISTER
	db 13, QUICK_ATTACK
	db 16, FOCUS_ENERGY
	db 19, WING_ATTACK
	db 23, AGILITY
	db 26, TAKE_DOWN
	db 29, BATON_PASS
	db 32, SKY_ATTACK
	db 35, DOUBLE_TEAM
	db 38, HYPER_VOICE
	db 41, DOUBLE_EDGE
	db 44, BRAVE_BIRD
	db 0 ; no more level-up moves

SwellowEvosAttacks:
	db 0 ; no more evolutions
	db 1, AIR_SLASH
	db 1, SUPERSONIC
	db 1, TWISTER
	db 1, QUICK_ATTACK
	db 1, FOCUS_ENERGY
	db 1, WING_ATTACK
	db 24, AGILITY
	db 28, TAKE_DOWN
	db 32, BATON_PASS
	db 36, SKY_ATTACK
	db 41, DOUBLE_TEAM
	db 46, HYPER_VOICE
	db 51, DOUBLE_EDGE
	db 56, BRAVE_BIRD
	db 0 ; no more level-up moves

RaltsEvosAttacks:
	db EVOLVE_LEVEL, 20, KIRLIA
	db 0 ; no more evolutions
	db 1, MUD_SLAP
	db 4, GROWL
	db 7, CONFUSION
	db 10, TELEPORT
	; db 13, DRAININGKISS
	db 16, SING
	db 19, DREAM_EATER
	db 22, FUTURE_SIGHT
	; db 25, SIGNAL_BEAM
	db 28, HYPNOSIS
	db 31, ZEN_HEADBUTT
	db 34, ENCORE
	db 37, DESTINY_BOND
	; db 40, CALM_MIND
	db 43, PSYCHIC
	db 46, PAIN_SPLIT
	db 0 ; no more level-up moves

MakuhitaEvosAttacks:
	db EVOLVE_LEVEL, 24, HARIYAMA
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, FOCUS_ENERGY
	db 7, REVERSAL
	db 10, SAND_ATTACK
	db 13, DOUBLESLAP
	db 16, SEISMIC_TOSS
	db 19, DYNAMICPUNCH
	db 22, FEINT_ATTACK
	db 25, BELLY_DRUM
	db 28, FORESIGHT
	db 31, CROSS_CHOP
	db 34, BODY_SLAM
	db 37, WHIRLWIND
	db 40, BULLET_PUNCH
	db 0 ; no more level-up moves

WhismurEvosAttacks:
	db EVOLVE_LEVEL, 20, LOUDRED
	db 0 ; no more evolutions
	db 1, POUND
	db 4, SUPERSONIC
	db 7, ASTONISH
	db 10, ROAR
	; db 13, LAUGHING_GAS
	; db 16, POWER_BALLAD
	db 19, BITE
	db 22, STOMP
	; db 25, THUNDER_FANG
	; db 28, NOISE_PULSE
	db 31, REST
	db 34, SLEEP_TALK
	db 37, ZEN_HEADBUTT
	db 40, HYPER_VOICE
	db 43, SCREECH
	db 46, OUTRAGE
	db 49, CRUNCH
	; db 52, BASE_TREMOR
	db 0 ; no more level-up moves

LoudredEvosAttacks:
	db EVOLVE_LEVEL, 40, EXPLOUD
	db 0 ; no more evolutions
	db 1, ROAR
	; db 1, LAUGHING_GAS
	; db 1, POWER_BALLAD
	db 1, BITE
	db 23, STOMP
	; db 27, THUNDER_FANG
	; db 31, NOISE_PULSE
	db 35, REST
	db 39, SLEEP_TALK
	db 43, ZEN_HEADBUTT
	db 47, HYPER_VOICE
	db 51, SCREECH
	db 55, OUTRAGE
	db 59, CRUNCH
	; db 63, BASE_TREMOR
	db 0 ; no more level-up moves

LotadEvosAttacks:
	db EVOLVE_LEVEL, 14, LOMBRE
	db 0 ; no more evolutions
	db 1, ASTONISH
	db 4, GROWL
	db 7, ABSORB
	db 10, BUBBLE
	db 13, MIST
	db 16, LEECH_SEED
	db 19, RAZOR_LEAF
	db 22, BUBBLEBEAM
	db 25, SYNTHESIS
	db 28, ZEN_HEADBUTT
	; db 31, SEED_BOMB
	db 34, HYDRO_PUMP
	; db 37, DRAIN_PUNCH
	; db 40, NATURE_POWER
	db 43, GIGA_DRAIN
	db 0 ; no more level-up moves

LombreEvosAttacks:
	db EVOLVE_ITEM, WATER_STONE, LUDICOLO
	db 0 ; no more evolutions
	db 1, GROWL
	db 1, ABSORB
	db 1, BUBBLE
	db 1, MIST
	db 17, LEECH_SEED
	db 21, RAZOR_LEAF
	db 25, BUBBLEBEAM
	db 29, SYNTHESIS
	db 33, ZEN_HEADBUTT
	; db 37, SEED_BOMB
	db 41, HYDRO_PUMP
	; db 45, DRAIN_PUNCH
	; db 49, NATURE_POWER
	db 53, GIGA_DRAIN
	db 0 ; no more level-up moves

LudicoloEvosAttacks:
	db 0 ; no more evolutions
	db 1, GROWL
	db 1, ABSORB
	db 1, BUBBLE
	db 1, MIST
	db 18, LEECH_SEED
	db 23, RAZOR_LEAF
	db 28, BUBBLEBEAM
	db 33, SYNTHESIS
	db 38, ZEN_HEADBUTT
	; db 43, SEED_BOMB
	db 48, HYDRO_PUMP
	; db 53, DRAIN_PUNCH
	; db 58, NATURE_POWER
	db 63, GIGA_DRAIN
	db 0 ; no more level-up moves

KirliaEvosAttacks:
	db EVOLVE_LEVEL, 30, GARDEVOIR
	; db EVOLVE_ITEM_MALE, DAWN_STONE, GALLADE  ; TODO: uncomment once added
	db 0 ; no more evolutions
	db 1, CONFUSE_RAY
	db 1, CHARM
	db 1, TELEPORT
	; db 1, DRAININGKISS
	db 1, SING
	db 1, DREAM_EATER
	db 23, FUTURE_SIGHT
	; db 27, SIGNAL_BEAM
	db 31, HYPNOSIS
	db 35, ZEN_HEADBUTT
	db 39, ENCORE
	db 43, DESTINY_BOND
	; db 47, CALM_MIND
	db 51, PSYCHIC
	db 55, PAIN_SPLIT
	db 0 ; no more level-up moves

HariyamaEvosAttacks:
	db 0 ; no more evolutions
	db 1, DOUBLESLAP
	db 1, SEISMIC_TOSS
	db 1, DYNAMICPUNCH
	db 1, FEINT_ATTACK
	db 26, BELLY_DRUM
	db 31, FORESIGHT
	db 36, CROSS_CHOP
	db 41, BODY_SLAM
	db 46, WHIRLWIND
	db 51, BULLET_PUNCH
	db 0 ; no more level-up moves

GardevoirEvosAttacks:
	db 0 ; no more evolutions
	db 1, SING
	db 1, DREAM_EATER
	db 1, FUTURE_SIGHT
	; db 1, SIGNAL_BEAM
	db 32, HYPNOSIS
	db 37, ZEN_HEADBUTT
	db 42, ENCORE
	; db 47, MOONBLAST
	; db 52, CALM_MIND
	db 57, PSYCHIC
	db 62, HEAL_BELL
	db 0 ; no more level-up moves


ExploudEvosAttacks:
	db 0 ; no more evolutions
	; db 1, THUNDER_FANG
	; db 1, NOISE_PULSE
	db 1, REST
	db 1, SLEEP_TALK
	db 44, ZEN_HEADBUTT
	db 49, HYPER_VOICE
	db 54, SCREECH
	db 59, OUTRAGE
	db 64, CRUNCH
	; db 69, BASE_TREMOR
	db 0 ; no more level-up moves

AronEvosAttacks:
	db EVOLVE_LEVEL, 32, LAIRON
	db 0 ; no more evolutions
	db 1, HARDEN
	db 1, TACKLE
	db 4, METAL_CLAW
	; db 8, ROCK_TOMB
	db 12, ROAR
	db 16, HEADBUTT
	db 20, PROTECT
	db 24, ROCK_SLIDE
	db 28, IRON_HEAD
	; db 33, METAL_SOUND
	db 36, TAKE_DOWN
	; db 40, AUTONOMIZE
	db 44, IRON_TAIL
	; db 48, IRON_DEFENSE
	; db 52, HEAVY_SLAM
	db 56, DOUBLE_EDGE
	; db 60, METAL_BURST
	db 0 ; no more level-up moves

LaironEvosAttacks:
	db EVOLVE_LEVEL, 42, AGGRON
	db 0 ; no more evolutions
	db 1, HARDEN
	db 1, METAL_CLAW
	; db 1, ROCK_TOMB
	db 1, TACKLE
	db 12, ROAR
	db 16, HEADBUTT
	db 20, PROTECT
	db 24, ROCK_SLIDE
	db 28, IRON_HEAD
	; db 35, METAL_SOUND
	db 40, TAKE_DOWN
	; db 46, AUTONOMIZE
	db 52, IRON_TAIL
	; db 58, IRON_DEFENSE
	; db 64, HEAVY_SLAM
	db 70, DOUBLE_EDGE
	; db 76, METAL_BURST
	db 0 ; no more level-up moves

AggronEvosAttacks:
	db 0 ; no more evolutions
	db 1, HARDEN
	db 1, METAL_CLAW
	; db 1, ROCK_TOMB
	db 1, TACKLE
	db 12, ROAR
	db 16, HEADBUTT
	db 20, PROTECT
	db 24, ROCK_SLIDE
	db 28, IRON_HEAD
	; db 35, METAL_SOUND
	db 40, TAKE_DOWN
	; db 48, AUTONOMIZE
	db 56, IRON_TAIL
	; db 64, IRON_DEFENSE
	; db 72, HEAVY_SLAM
	db 80, DOUBLE_EDGE
	; db 88, METAL_BURST
	db 0 ; no more level-up moves

WingullEvosAttacks:
	db EVOLVE_LEVEL, 25, PELIPPER
	db 0 ; no more evolutions
	db 1, GROWL
	db 1, WATER_GUN
	db 7, SUPERSONIC
	db 13, WING_ATTACK
	db 21, MIST
	db 31, QUICK_ATTACK
	db 43, PURSUIT
	db 55, AGILITY
	db 0 ; no more level-up moves

PelipperEvosAttacks:
	db 0 ; no more evolutions
	db 1, GROWL
	db 1, WATER_GUN
	db 7, SUPERSONIC
	db 13, WING_ATTACK
	db 21, MIST
	db 25, PROTECT
	db 43, PURSUIT
	db 61, HYDRO_PUMP
	db 0 ; no more level-up moves

ShroomishEvosAttacks:
	db EVOLVE_LEVEL, 23, BRELOOM
	db 0 ; no more evolutions
	db 1, ABSORB
	db 4, TACKLE
	db 7, STUN_SPORE
	db 10, LEECH_SEED
	db 16, MEGA_DRAIN
	db 22, HEADBUTT
	db 28, POISONPOWDER
	db 36, GROWTH
	db 45, GIGA_DRAIN
	db 54, SPORE
	db 0 ; no more level-up moves

BreloomEvosAttacks:
	db 0 ; no more evolutions
	db 1, ABSORB
	db 1, LEECH_SEED
	db 1, STUN_SPORE
	db 1, TACKLE
	db 7, STUN_SPORE
	db 10, LEECH_SEED
	db 16, MEGA_DRAIN
	db 22, HEADBUTT
	db 23, MACH_PUNCH
	db 28, COUNTER
	;db 36, sky uppercut
	;db 45, mind reader
	db 54, DYNAMIC_PUNCH
	db 0 ; no more level-up moves

SlakothEvosAttacks:
	db EVOLVE_LEVEL, 18, VIGOROTH
	db 0 ; no more evolutions
	db 1, SCRATCH
	; db 1, YAWN
	db 7, ENCORE
	; db 13, SLACK_OFF
	db 19, FEINT_ATTACK
	db 25, AMNESIA
	; db 31, COVET
	db 37, COUNTER
	db 43, FLAIL
	db 0 ; no more level-up moves

VigorothEvosAttacks:
	db EVOLVE_LEVEL, 36, SLAKING
	db 0 ; no more evolutions
	db 1, ENCORE
	db 1, FOCUS_ENERGY
	db 1, SCRATCH
	; db 1, UPROAR
	db 7, ENCORE
	; db 13, UPROAR
	db 19, FURY_SWIPES
	db 25, ENDURE
	db 31, SLASH
	db 37, COUNTER
	; db 43, FOCUS_PUNCH
	db 49, REVERSAL
	db 0 ; no more level-up moves

SlakingEvosAttacks:
	db 0 ; no more evolutions
	db 1, ENCORE
	db 1, SCRATCH
	; db 1, SLACK_OFF
	; db 1, YAWN
	db 7, ENCORE
	; db 13, SLACK_OFF
	db 19, FEINT_ATTACK
	db 25, AMNESIA
	; db 31, COVET
	db 36, SWAGGER
	db 37, COUNTER
	db 43, FLAIL
	db 0 ; no more level-up moves

PoochyenaEvosAttacks:
	db EVOLVE_LEVEL, 18, MIGHTYENA
	db 0 ; no more evolutions
	db 1, TACKLE
	; db 5, HOWL
	db 9, SAND_ATTACK
	db 13, BITE
	; db 17, ODOR_SLEUTH
	db 21, ROAR
	db 25, SWAGGER
	db 29, SCARY_FACE
	db 33, TAKE_DOWN
	; db 37, TAUNT
	db 41, CRUNCH
	db 45, THEIF
	db 0 ; no more level-up moves

MightyenaEvosAttacks:
	db 0 ; no more evolutions
	db 1, BITE
	; db 1, HOWL
	db 1, SAND_ATTACK
	db 1, TACKLE
	; db 5, HOWL
	db 9, SAND_ATTACK
	db 13, BITE
	; db 17, ODOR_SLEUTH
	db 22, ROAR
	db 27, SWAGGER
	db 32, SCARY_FACE
	db 37, TAKE_DOWN
	; db 42, TAUNT
	db 47, CRUNCH
	db 52, THEIF
	db 0 ; no more level-up moves

SurskitEvosAttacks:
	db EVOLVE_LEVEL, 22, MASQUERAIN
	db 0 ; no more evolutions
	db 1, QUICK_ATTACK
	db 4, SWEET_SCENT
	db 7, BUBBLE
	db 10, STUN_SPORE
	db 13, MIST
	db 16, RAIN_DANCE
	db 19, AQUA_JET
	db 23, PSYBEAM
	; db 26, SIGNAL_BEAM
	db 29, STRING_SHOT
	db 32, BUBBLEBEAM
	db 35, HAZE
	db 38, BUG_BUZZ
	db 41, AGILITY
	db 44, HYDRO_PUMP
	db 47, BATON_PASS
	db 0 ; no more level-up moves

MasquerainEvosAttacks:
	db 0 ; no more evolutions
	db 1, FORESIGHT
	db 1, MIND_READER
	db 1, TWISTER
	db 1, STUN_SPORE
	db 1, MIST
	db 1, RAIN_DANCE
	db 1, AQUA_JET
	db 22, GUST
	db 23, PSYBEAM
	; db 26, SIGNAL_BEAM
	db 29, SCARY_FACE
	db 32, AIR_SLASH
	db 35, HAZE
	db 38, BUG_BUZZ
	db 41, AGILITY
	; db 44, STORM_FRONT
	db 47, BATON_PASS
	db 0 ; no more level-up moves

SableyeEvosAttacks:
	db 0 ; no more evolutions
	db 1, SCRATCH
	db 3, FORESIGHT
	db 7, SPITE
	db 10, ASTONISH
	db 14, LEER
	db 18, SWIFT
	db 22, FEINT_ATTACK
	db 26, PAIN_SPLIT
	db 30, CONFUSE_RAY
	db 34, SHADOW_BALL
	db 38, NASTY_PLOT
	db 42, DARK_PULSE
	db 45, NIGHT_SHADE
	db 48, POWER_GEM
	db 51, ZEN_HEADBUTT
	db 54, SHADOW_CLAW
	db 57, NIGHT_SLASH
	db 60, RECOVER
	; db 63, WILL_O_WISP
	db 0 ; no more level-up moves

SnoverEvosAttacks:
	db 0 ; no more evolutions
	db 1, LEER
	db 1, POWDER_SNOW
	; db 5, LEAFAGE
	db 10, MIST
	; db 15, ICE_SHARD
	db 20, RAZOR_LEAF
	db 25, ICY_WIND
	db 30, SWAGGER
	; db 35, INGRAIN
	; db 41, WOOD_HAMMER
	db 45, BLIZZARD
	db 50, SHEER_COLD
	db 0 ; no more level-up moves

MimikyuEvosAttacks:
	db 0 ; no more evolutions
	db 1, ASTONISH
	; db 1, COPYCAT
	db 1, SCRATCH
	db 1, SPLASH
	; db 1, WOOD_HAMMER
	; db 6, SHADOW_SNEAK
	db 12, DOUBLE_TEAM
	; db 18, BABY_DOLL_EYES
	db 24, MIMIC
	; db 30, HONE_CLAWS
	db 36, SLASH
	db 42, SHADOW_CLAW
	db 48, CHARM
	db 54, PLAY_ROUGH
	db 60, PAIN_SPLIT
	db 0 ; no more level-up moves

CorphishEvosAttacks:
	db EVOLVE_LEVEL, 30, CRAWDAUNT
	db 0 ; no more evolutions
	db 1, HARDEN
	db 1, WATER_GUN
	db 4, LEER
	db 8, TAUNT
	db 12, BUBBLEBEAM
	db 16, KNOCK_OFF
	; db 20, DOUBLE_HIT
	db 24, PROTECT
	db 28, NIGHT_SLASH
	; db 32, RAZOR_SHELL
	db 36, SWORDS_DANCE
	db 40, CRUNCH
	db 44, CRABHAMMER
	; db 48, ENDEAVOR
	db 52, GUILLOTINE
	db 0 ; no more level-up moves

CrawdauntEvosAttacks:
	db 0 ; no more evolutions
	db 1, HARDEN
	db 1, LEER
	db 1, SWIFT
	; db 1, TAUNT
	db 1, WATER_GUN
	db 12, BUBBLEBEAM
	db 16, KNOCK_OFF
	; db 20, DOUBLE_HIT
	db 24, PROTECT
	db 28, NIGHT_SLASH
	; db 34, RAZOR_SHELL
	db 40, SWORDS_DANCE
	db 46, CRUNCH
	db 52, CRABHAMMER
	; db 58, ENDEAVOR
	db 64, GUILLOTINE
	db 0 ; no more level-up moves

LitwickEvosAttacks:
	db EVOLVE_LEVEL, 41, LAMPENT
	db 0 ; no more evolutions
	; db 1, ASTONISH
	db 1, SMOG
	db 4, EMBER
	db 8, MINIMIZE
	db 12, CONFUSE_RAY
	; db 16, HEX
	; db 20, WILL_O_WISP
	db 24, FIRE_SPIN
	db 28, NIGHT_SHADE
	db 32, CURSE
	db 36, SHADOW_BALL
	; db 40, INFERNO
	; db 44, IMPRISON
	db 48, PAIN_SPLIT
	; db 52, OVERHEAT
	; db 56, MEMENTO
	db 0 ; no more level-up moves

LampentEvosAttacks:
	db EVOLVE_ITEM, DUSK_STONE, CHANDELURE
	db 0 ; no more evolutions
	; db 1, ASTONISH
	db 1, SMOG
	db 4, EMBER
	db 8, MINIMIZE
	db 12, CONFUSE_RAY
	; db 16, HEX
	; db 20, WILL_O_WISP
	db 24, FIRE_SPIN
	db 28, NIGHT_SHADE
	db 32, CURSE
	db 36, SHADOW_BALL
	; db 40, INFERNO
	; db 44, IMPRISON
	db 48, PAIN_SPLIT
	; db 52, OVERHEAT
	; db 56, MEMENTO
	db 0 ; no more level-up moves

ChandelureEvosAttacks:
	db 0 ; no more evolutions
	; db 1, ASTONISH
	db 1, CONFUSE_RAY
	db 1, CURSE
	db 1, EMBER
	db 1, FIRE_SPIN
	; db 1, HEX
	; db 1, IMPRISON
	; db 1, INFERNO
	; db 1, MEMENTO
	db 1, MINIMIZE
	db 1, NIGHT_SHADE
	; db 1, OVERHEAT
	db 1, PAIN_SPLIT
	db 1, SHADOW_BALL
	db 1, SMOG
	; db 1, WILL_O_WISP
	db 0 ; no more level-up moves

DwebbleEvosAttacks:
	db EVOLVE_LEVEL, 34, CRUSTLE
	db 0 ; no more evolutions
	db 1, FURY_CUTTER
	db 1, SAND_ATTACK
	db 4, WITHDRAW
	; db 8, SMACK_DOWN
	; db 12, BUG_BITE
	db 16, FLAIL
	db 20, SLASH
	db 24, ROCK_SLIDE
	; db 28, STEALTH_ROCK
	; db 32, ROCK_BLAST
	; db 36, X_SCISSOR
	; db 40, ROCK_POLISH
	; db 44, SHELL_SMASH
	; db 48, ROCK_WRECKER
	db 0 ; no more level-up moves

CrustleEvosAttacks:
	db 0 ; no more evolutions
	db 1, FURY_CUTTER
	db 1, SAND_ATTACK
	; db 1, SMACK_DOWN
	db 1, WITHDRAW
	; db 12, BUG_BITE
	db 16, FLAIL
	db 20, SLASH
	db 24, ROCK_SLIDE
	; db 28, STEALTH_ROCK
	; db 32, ROCK_BLAST
	; db 36, X_SCISSOR
	; db 40, ROCK_POLISH
	; db 44, SHELL_SMASH
	; db 48, ROCK_WRECKER
	db 0 ; no more level-up moves

ScorbunnyEvosAttacks:
	db EVOLVE_LEVEL, 16, RABOOT
	db 0 ; no more evolutions
	db 1, GROWL
	db 1, TACKLE
	db 6, EMBER
	db 8, QUICK_ATTACK
	db 12, DOUBLE_KICK
	; db 17, FLAME_CHARGE
	db 20, AGILITY
	db 24, HEADBUTT
	db 28, COUNTER
	; db 32, BOUNCE
	db 36, DOUBLE_EDGE
	db 0 ; no more level-up moves

RabootEvosAttacks:
	db 0 ; no more evolutions
	db 1, EMBER
	db 1, GROWL
	db 1, TACKLE
	db 1, QUICK_ATTACK
	db 12, DOUBLE_KICK
	; db 19, FLAME_CHARGE
	db 24, AGILITY
	db 30, HEADBUTT
	db 36, COUNTER
	; db 42, BOUNCE
	db 48, DOUBLE_EDGE
	db 0 ; no more level-up moves

RegielekiEvosAttacks:
	db 0 ; no more evolutions
	db 1, RAPID_SPIN
	db 1, THUNDER_SHOCK
	; db 6, ELECTROWEB
	db 12, ANCIENTPOWER
	; db 18, SHOCK_WAVE
	db 24, THUNDER_WAVE
	db 30, EXTREMESPEED
	db 36, THUNDER_CAGE
	db 42, THUNDERBOLT
	; db 48, MAGENT_RISE
	db 54, THRASH
	db 60, LOCK_ON
	db 66, ZAP_CANNON
	db 72, HYPER_BEAM
	db 78, EXPLOSION
	db 0 ; no more level-up moves

RegidragoEvosAttacks:
	db 0 ; no more evolutions
	db 1, TWISTER
	db 1, VICE_GRIP
	db 6, BITE
	db 12, ANCIENTPOWER
	db 18, DRAGONBREATH
	db 24, FOCUS_ENERGY
	db 30, CRUNCH
	; db 36, DRAGON_CLAW
	; db 42, HAMMER_ARM
	; db 48, DRAGON_DANCE
	db 54, THRASH
	; db 60, LASER_FOCUS
	db 66, DRAGON_ENERGY
	db 72, HYPER_BEAM
	db 78, EXPLOSION
	db 0 ; no more level-up moves

RegirockEvosAttacks:
	db 0 ; no more evolutions
	; db 1, CHARGE_BEAM
	db 1, ROCK_THROW
	; db 6, BULLDOZE
	db 12, ANCIENTPOWER
	db 18, STOMP
	db 24, ROCK_SLIDE
	db 30, CURSE
	; db 36, IRON_DEFENSE
	; db 42, HAMMER_ARM
	; db 48, STONE_EDGE
	; db 54, STONE_EDGE
	db 60, LOCK_ON
	db 66, ZAP_CANNON
	db 72, HYPER_BEAM
	db 78, EXPLOSION
	db 0 ; no more level-up moves

RegiceEvosAttacks:
	db 0 ; no more evolutions
	; db 1, CHARGE_BEAM
	db 1, ICY_WIND
	; db 6, BULLDOZE
	db 12, ANCIENTPOWER
	db 18, STOMP
	db 24, ICE_BEAM
	db 30, CURSE
	db 36, AMNESIA
	; db 42, HAMMER_ARM
	db 48, BLIZZARD
	; db 54, SUPERPOWER
	db 60, LOCK_ON
	db 66, ZAP_CANNON
	db 72, HYPER_BEAM
	db 78, EXPLOSION
	db 0 ; no more level-up moves

RegisteelEvosAttacks:
	db 0 ; no more evolutions
	; db 1, CHARGE_BEAM
	db 1, METAL_CLAW
	; db 6, BULLDOZE
	db 12, ANCIENTPOWER
	db 18, STOMP
	db 24, FLASH_CANNON
	db 24, IRON_HEAD
	db 30, CURSE
	db 36, AMNESIA
	; db 36, IRON_DEFENSE
	; db 42, HAMMER_ARM
	; db 48, HEAVY_SLAM
	; db 54, SUPERPOWER
	db 60, LOCK_ON
	db 66, ZAP_CANNON
	db 72, HYPER_BEAM
	db 78, EXPLOSION
	db 0 ; no more level-up moves

RegigigasEvosAttacks:
	db 0 ; no more evolutions
	db 1, CONFUSE_RAY
	db 1, POUND
	; db 6, PAYBACK
	; db 12, REVENGE
	db 18, STOMP
	db 24, PROTECT
	; db 30, KNOCK_OFF
	db 36, MEGA_PUNCH
	; db 42, BODY_PRESS
	; db 48, WIDE_GUARD
	; db 54, ZEN_HEADBUTT
	; db 60, HEAVY_SLAM
	; db 66, HAMMER_ARM
	; db 72, GIGA_IMPACT
	db 78, CRUSH_GRIP
	db 0 ; no more level-up moves

BronzorEvosAttacks:
	db EVOLVE_LEVEL, 33, BRONZONG
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

BronzongEvosAttacks:
	db 0 ; no more evolutions
	db 1, CONFUSE_RAY
	db 1, FLASH_CANNON
	db 1, ZEN_HEADBUTT
	db 41, IRON_HEAD
	db 46, IRON_DEFENSE
	db 51, PSYCHIC
	db 0 ; no more level-up moves

DarumakaEvosAttacks:
	; db EVOLVE_LEVEL, 33, BRONZONG
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

ElgyemEvosAttacks:
	; db EVOLVE_LEVEL, 33, BRONZONG
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

CutieflyEvosAttacks:
	; db EVOLVE_LEVEL, 33, BRONZONG
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

RibombeeEvosAttacks:
	; db EVOLVE_LEVEL, 33, BRONZONG
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

BeheeyemEvosAttacks:
	; db EVOLVE_LEVEL, 33, BRONZONG
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

SandileEvosAttacks:
	db EVOLVE_LEVEL, 11, KROKOROK
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

KrokorokEvosAttacks:
	db EVOLVE_LEVEL, 22, Krookodile
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

KrookodileEvosAttacks:
	db 0 ; no more evolutions
	db 1, TACKLE
	db 4, DISABLE
	db 7, CONFUSION
	db 10, HYPNOSIS
	db 13, FUTURE_SIGHT
	db 17, FEINT_ATTACK
	db 20, PSYBEAM
	db 23, CONFUSE_RAY
	db 26, FLASH_CANNON
	db 32, ZEN_HEADBUTT
	db 38, IRON_HEAD
	db 41, IRON_DEFENSE
	db 44, PSYCHIC
	db 0 ; no more level-up moves

