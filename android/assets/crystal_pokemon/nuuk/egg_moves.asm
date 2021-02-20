INCLUDE "constants.asm"


SECTION "Egg Moves", ROMX

; All instances of Charm, Steel Wing, Sweet Scent, and Lovely Kiss were
; removed from egg move lists in Crystal.
; Sweet Scent and Steel Wing were redundant since they're TMs, and
; Charm and Lovely Kiss were unobtainable.

; Staryu's egg moves were removed in Crystal, because Staryu is genderless
; and can only breed with Ditto.

INCLUDE "data/pokemon/egg_move_pointers.asm"


TaillowEggMoves:
	db MIRROR_MOVE

AronEggMoves:
	db SPITE
	db REVERSAL
	db CRYSTAL_BOLT

LotadEggMoves:
	db MEGA_DRAIN
	db WATER_GUN

MakuhitaEggMoves:

RaltsEggMoves:
	db DISABLE
	db CONFUSE_RAY
	db CHARM
	db MEAN_LOOK

WhismurEggMoves:

WingullEggMoves:
	db GUST
	db TWISTER

ShroomishEggMoves:
	db CHARM
	db FALSE_SWIPE
	db SWAGGER

SlakothEggMoves:
	db BODY_SLAM
	; db CRUSH_CLAW
	db CURSE
	db PURSUIT
	db SLASH
	db SLEEP_TALK
	db SNORE

PoochyenaEggMoves:
	; db ASTONISH
	; db COVENT
	db LEER
	; db POISON_FANG
	; db YAWN

SableyeEggMoves:

SurskitEggMoves:
