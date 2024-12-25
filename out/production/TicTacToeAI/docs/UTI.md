# Overview
UTI stands for Universal TicTacToe Interface which is going to be used for TicTacToe engines

# Commands

Bare minimums
- go
- stop
- position
- bench

# go 
The go commands must be sent as follows
`go xTime n oTime n xInc n oInc n`<br/>
(The `n` stands relative for the milliseconds for the time and the increment in milliseconds)

- `xTime` This is the time in milliseconds for `x`
- `oTime` This is the time in milliseconds for `o`
- `xInc` This is the increment in milliseconds for `x`
- `oInc` This is the increment in milliseconds for `o`

# stop
This must stop the search meaning the UTI must run on a different Thread then the search

# position
After this command a String with the format starting at the top left ``0`` is for ``o`` and ``1`` stands for ``x``.<br/>
After this position information either `x` or `o` representing the side to move in the current position.

# bench
Representing the node count on a few position.<br/>This is used to determine if something is wrong like an unstable bench.
<br/>Also you can detect a functional and a no functional meaning if the bench has changed.<br/> Also the `NPS (Nodes Per Second)` 
is usefully to determine the time scaling on other machines based on a predefined NPS.