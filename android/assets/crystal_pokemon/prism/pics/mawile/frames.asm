	dw .frame1
	dw .frame2
	dw .frame3
	dw .frame4
	dw .frame5
	dw .frame6
.frame1
	db $00 ; bitmask
	db $31, $32, $33, $34
.frame2
	db $00 ; bitmask
	db $31, $35, $33, $36
.frame3
	db $01 ; bitmask
	db $37, $38, $39, $3a, $3b, $3c, $3d, $3e, $3f, $40, $41, $31, $42, $33, $36
.frame4
	db $02 ; bitmask
	db $43, $44, $45, $46, $47, $48, $49, $4a, $4b, $4c, $4d, $4e
.frame5
	db $03 ; bitmask
.frame6
	db $04 ; bitmask
	db $4f, $50, $51, $52, $53, $54, $55, $56, $57, $58
