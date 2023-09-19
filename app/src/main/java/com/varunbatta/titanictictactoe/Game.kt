package com.varunbatta.titanictictactoe

class Game(
    var level: Int,
    var player1: Player,
    var player2: Player,
) {
    var data = Array(10) { i -> Array(9) { j -> ""} }
    var lastMove = ""
    var lastMoveRow = -1
    var lastMoveColumn = -1
}