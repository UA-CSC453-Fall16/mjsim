/*** Start of Header ***/
	.file	"post_dec.cpp"
__SREG__ = 0x3f
__SP_H__ = 0x3e
__SP_L__ = 0x3d
__CCP__  = 0x34
__tmp_reg__ = 0
__zero_reg__ = 1
	.global __do_copy_data
	.global __do_clear_bss
	.text
.global	main
	.type	main, @function
main:
	push r29
	push r28
	rcall .
	in r28,__SP_L__
	in r29,__SP_H__
/* prologue: function */
/* frame size = 2 */
/*** End of Header ***/



; char x = 10;
	ldi r24,lo8(10)	; Load 10 into a register
	std Y+2,r24	; Store the contents of the register into x

; char y = x--;
	ldd r24,Y+2	; Load x into a register (do assignment first)
	std Y+1,r24	; Store the result into y
	ldd r24,Y+2	; Load x again
	subi r24,lo8(1)	; Subtract 1 from the register
	std Y+2,r24	; Store the result back into x

; return 0;
	ldi r24,lo8(0)
	ldi r25,hi8(0)



/*** Start of Footer ***/
/* epilogue start */
	pop __tmp_reg__
	pop __tmp_reg__
	pop r28
	pop r29
	ret
	.size	main, .-main
/*** End of Footer ***/
