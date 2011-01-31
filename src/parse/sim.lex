package parse;
import java_cup.runtime.Symbol;
import parse.TokenValue;

%%
%line
%char
%cup
%public

%eofval{
  return new Symbol(sym.EOF);
%eofval}

DIGIT=[0-9]
NOT_ZERO=[1-9]
NUMBER=([-+]?{NOT_ZERO}{DIGIT}*)|0
HEX=0x[0-9A-Fa-f]+
LETTER=[A-Za-z]
ID=[_A-Za-z][_A-Za-z0-9]+
QSTRING=\"[^\"]*\"

WHITESPACE=[ \t\n\r\f]
EOL=(\n+|\r+|\r\n+)
NOT_EOL=[^\r\n]
LINE_COMMENT=[;#]{NOT_EOL}*
C_COMMENT="/*"[^*]*("*"([^*/][^*]*)?)*"*/"

REG=([Rr]{DIGIT}+)|[XxYyZz]
GLOBAL=__{ID}__
LABEL={ID}:
TYPE=@{LETTER}+

%%

"," { return new Symbol(sym.COMMA, new TokenValue(",", yyline, yychar)); }
"=" { return new Symbol(sym.ASSIGN, new TokenValue("=", yyline, yychar)); }

".file" { return new Symbol(sym.DOT_FILE, new TokenValue(".file", yyline, yychar)); }
".global" { return new Symbol(sym.DOT_GLOBAL, new TokenValue(".global", yyline, yychar)); }
".text" { return new Symbol(sym.DOT_TEXT, new TokenValue(".text", yyline, yychar)); }
".type" { return new Symbol(sym.DOT_TYPE, new TokenValue(".type", yyline, yychar)); }

"__SP_L__" { return new Symbol(sym.SP_L, new TokenValue("__SP_L__", yyline, yychar)); }
"__SP_H__" { return new Symbol(sym.SP_H, new TokenValue("__SP_H__", yyline, yychar)); }

"push" { return new Symbol(sym.AVR_PUSH, new TokenValue("push", yyline, yychar)); }
"pop" { return new Symbol(sym.AVR_POP, new TokenValue("pop", yyline, yychar)); }
"cp" { return new Symbol(sym.AVR_CP, new TokenValue("cp", yyline, yychar)); }
"sub" { return new Symbol(sym.AVR_SUB, new TokenValue("sub", yyline, yychar)); }
"sbc" { return new Symbol(sym.AVR_SBC, new TokenValue("sbc", yyline, yychar)); }
"breq" { return new Symbol(sym.AVR_BREQ, new TokenValue("breq", yyline, yychar)); }
"brlo" { return new Symbol(sym.AVR_BRLO, new TokenValue("brlo", yyline, yychar)); }
"brlt" { return new Symbol(sym.AVR_BRLT, new TokenValue("brlt", yyline, yychar)); }
"tst" { return new Symbol(sym.AVR_TST, new TokenValue("txt", yyline, yychar)); }
"muls" { return new Symbol(sym.AVR_MULS, new TokenValue("muls", yyline, yychar)); }
"in" { return new Symbol(sym.AVR_IN, new TokenValue("in", yyline, yychar)); }
"ld" { return new Symbol(sym.AVR_LD, new TokenValue("ld", yyline, yychar));  }
"ldi" { return new Symbol(sym.AVR_LDI, new TokenValue("ldi", yyline, yychar)); }
"add" { return new Symbol(sym.AVR_ADD, new TokenValue("add", yyline, yychar)); }
"lo8" { return new Symbol(sym.AVR_LO, new TokenValue("lo8", yyline, yychar)); }
"hi8" { return new Symbol(sym.AVR_HI, new TokenValue("hi8", yyline, yychar)); }
"adc" { return new Symbol(sym.AVR_ADC, new TokenValue("adc", yyline, yychar)); }
"and" { return new Symbol(sym.AVR_AND, new TokenValue("and", yyline, yychar)); }
"eor" { return new Symbol(sym.AVR_EOR, new TokenValue("eor", yyline, yychar)); }
"jmp" { return new Symbol(sym.AVR_JMP, new TokenValue("jmp", yyline, yychar)); }
"neg" { return new Symbol(sym.AVR_NEG, new TokenValue("neg", yyline, yychar)); }
"or" { return new Symbol(sym.AVR_OR, new TokenValue("or", yyline, yychar)); }
"call" { return new Symbol(sym.AVR_CALL, new TokenValue("call", yyline, yychar)); }
"ret" { return new Symbol(sym.AVR_RET, new TokenValue("ret", yyline, yychar)); }
".size" { return new Symbol(sym.SIZE_TYPE, new TokenValue(".size",yyline,yychar)); }
"-" { return new Symbol(sym.MINUS, new TokenValue("-",yyline,yychar)); }
"." { return new Symbol(sym.DOT, new TokenValue(".",yyline,yychar)); }
"(" { return new Symbol(sym.LPARAN, new TokenValue("(.",yyline,yychar)); }
")" { return new Symbol(sym.RPARAN, new TokenValue(")",yyline,yychar)); }

{NUMBER} { return new Symbol(sym.NUM, new TokenValue(yytext(), yyline, yychar)); }
{HEX} { return new Symbol(sym.HEX, new TokenValue(yytext(), yyline, yychar)); }
{QSTRING} { return new Symbol(sym.QSTRING, new TokenValue(yytext(), yyline, yychar)); }

{REG} { return new Symbol(sym.REG, new TokenValue(yytext().substring(1), yyline, yychar)); }
{GLOBAL} { return new Symbol(sym.GLOBAL, new TokenValue(yytext(), yyline, yychar)); }
{LABEL} { return new Symbol(sym.LABEL, new TokenValue(yytext(), yyline, yychar)); }
{TYPE} { return new Symbol(sym.TYPE, new TokenValue(yytext(), yyline, yychar)); }
{ID} { return new Symbol(sym.ID, new TokenValue(yytext(), yyline, yychar)); }
{EOL} { yychar = 0; return new Symbol(sym.EOL); }
{WHITESPACE} {}
{LINE_COMMENT} { yychar = 0; yy_buffer_start = yy_buffer_index-1; return new Symbol(sym.EOL);}
{C_COMMENT} { yychar = 0; return new Symbol(sym.EOL); }

. { System.out.println("Unexpected token '" + yytext() + "' on line: " + yyline + " at char: " + yychar); }
