package com.loloof64.chessexercisesorganizer.ui.components.moves_navigator

data class VariationToProcess(val firstMoveIndex: Int, val firstMoveText: String)
data class NodeSearchParam(val index: Int, val variationLevel: Int)
data class NodeSearchResult(
    val index: Int,
    val variationLevel: Int,
    val mainVariationMoveText: String?,
    val hasJustMetOpenParenthesis: Boolean,
    val hasJustMetMoveNumberAndOpenParenthesis: Boolean,
    val hasJustMetCloseParenthesis: Boolean,
    val variationsToProcess: List<VariationToProcess>,
    val isLastMoveNodeOfMainVariation: Boolean,
)

data class InnerPreviousNodeSearchParam(
    var currentNodeIndex: Int,
    var currentVariationLevel: Int,
    var previousTextIsOpenParenthesis: Boolean,
    var previousTextIsEndParenthesis: Boolean,
    var skippingSiblingVariation: Boolean,
    var needingToSkipOneMove: Boolean,
    var mustBreakLoop: Boolean,
    var hasJustMetOpenParenthesis: Boolean,
    var hasJustMetCloseParenthesis: Boolean,
    var hasJustMetMoveNumberAndOpenParenthesis: Boolean,
    val isLastMoveNodeOfMainVariation: Boolean,
)


data class InnerNextNodeSearchParam(
    var currentNodeIndex: Int,
    var currentVariationLevel: Int,
    var hasJustMetCloseParenthesis: Boolean,
    var hasJustMetOpenParenthesis: Boolean,
    var hasJustMetCorrectLevelMoveNumberAndOpenParenthesis: Boolean,
    var mustBreakLoop: Boolean,
    var mainVariationMoveText: String?,
    var variationsToProcess: MutableList<VariationToProcess>,
    val isLastMoveNodeOfMainVariation: Boolean,
)

fun updateInnerNodeSearchParamForNextNodeSearch(
    historyMoves: List<MovesNavigatorElement>,
    innerNextNodeSearchParam: InnerNextNodeSearchParam,
    selectedNodeVariationLevel: Int,
): InnerNextNodeSearchParam {
    val innerNodeSearchParamCopy = innerNextNodeSearchParam.copy(mustBreakLoop = false)
    val currentNode = historyMoves[innerNodeSearchParamCopy.currentNodeIndex]
    val sameLevelAsWhenStarting =
        innerNodeSearchParamCopy.currentVariationLevel == selectedNodeVariationLevel
    when {
        currentNode.text == ")" -> {
            innerNodeSearchParamCopy.hasJustMetCloseParenthesis = true
            innerNodeSearchParamCopy.hasJustMetOpenParenthesis = false
            innerNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis = false
            if (sameLevelAsWhenStarting) {
                innerNodeSearchParamCopy.currentNodeIndex--
                innerNodeSearchParamCopy.mustBreakLoop = true
            } else innerNodeSearchParamCopy.currentVariationLevel--
        }
        currentNode.text == "(" -> {
            innerNodeSearchParamCopy.currentVariationLevel++
            innerNodeSearchParamCopy.mustBreakLoop = false
            innerNodeSearchParamCopy.hasJustMetOpenParenthesis = true
            innerNodeSearchParamCopy.hasJustMetCloseParenthesis = false
            innerNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis = false
        }
        currentNode.fen != null -> {
            innerNodeSearchParamCopy.hasJustMetOpenParenthesis = false
            innerNodeSearchParamCopy.hasJustMetCloseParenthesis = false
            innerNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis = false
            if (sameLevelAsWhenStarting) {
                innerNodeSearchParamCopy.mustBreakLoop = true
            }
        }
        else -> {
            innerNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis =
                currentNode.text.isMoveNumber() && innerNodeSearchParamCopy.hasJustMetOpenParenthesis
            innerNodeSearchParamCopy.hasJustMetOpenParenthesis = false
            innerNodeSearchParamCopy.hasJustMetCloseParenthesis = false
            innerNodeSearchParamCopy.mustBreakLoop = false
        }
    }
    return innerNodeSearchParamCopy
}

fun updateInnerNodeSearchParamForCurrentLevelVariationsSearch(
    historyMoves: List<MovesNavigatorElement>,
    innerNextNodeSearchParam: InnerNextNodeSearchParam,
    selectedNodeVariationLevel: Int,
): InnerNextNodeSearchParam {
    val innerNextNodeSearchParamCopy = innerNextNodeSearchParam.copy()
    val currentNode = historyMoves[innerNextNodeSearchParamCopy.currentNodeIndex]
    val sameLevelAsWhenStarting =
        innerNextNodeSearchParamCopy.currentVariationLevel == selectedNodeVariationLevel
    val isJustOneVariationLevelUpAsWhenStarting =
        innerNextNodeSearchParamCopy.currentVariationLevel == selectedNodeVariationLevel + 1

    when {
        currentNode.text == ")" -> {
            innerNextNodeSearchParamCopy.hasJustMetCloseParenthesis = true
            innerNextNodeSearchParamCopy.hasJustMetOpenParenthesis = false
            innerNextNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis = false
            innerNextNodeSearchParamCopy.mustBreakLoop = false
            innerNextNodeSearchParamCopy.currentVariationLevel--
        }
        currentNode.text == "(" -> {
            innerNextNodeSearchParamCopy.currentVariationLevel++ 
            innerNextNodeSearchParamCopy.hasJustMetOpenParenthesis = true
            innerNextNodeSearchParamCopy.hasJustMetCloseParenthesis = false
            innerNextNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis = false
            innerNextNodeSearchParamCopy.mustBreakLoop = false
        }
        currentNode.fen != null -> {
            if (isJustOneVariationLevelUpAsWhenStarting) {
                if (innerNextNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis) {
                    innerNextNodeSearchParamCopy.variationsToProcess.add(
                        VariationToProcess(
                            firstMoveIndex = innerNextNodeSearchParamCopy.currentNodeIndex,
                            firstMoveText = currentNode.text,
                        )
                    )
                    innerNextNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis = false
                }
            }
            else {
                innerNextNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis = false
            }
            innerNextNodeSearchParamCopy.hasJustMetOpenParenthesis = false
            innerNextNodeSearchParamCopy.hasJustMetCloseParenthesis = false
            if (sameLevelAsWhenStarting) {
                innerNextNodeSearchParamCopy.mustBreakLoop = true
            }
        }
        else -> {
            innerNextNodeSearchParamCopy.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis =
                currentNode.text.isMoveNumber() && innerNextNodeSearchParamCopy.hasJustMetOpenParenthesis
            innerNextNodeSearchParamCopy.hasJustMetOpenParenthesis = false
            innerNextNodeSearchParamCopy.hasJustMetCloseParenthesis = false
            innerNextNodeSearchParamCopy.mustBreakLoop = false
        }
    }
    return innerNextNodeSearchParamCopy
}


fun searchForCurrentLevelVariations(
    historyMoves: List<MovesNavigatorElement>,
    innerNextNodeSearchParam: InnerNextNodeSearchParam,
    selectedNodeVariationLevel: Int,
): InnerNextNodeSearchParam {
    var innerNextNodeSearchParamCopy = innerNextNodeSearchParam.copy(
        mustBreakLoop = false,
        variationsToProcess = mutableListOf(),
    )

    while (true) {
        innerNextNodeSearchParamCopy.currentNodeIndex++

        val isPastLastNode = innerNextNodeSearchParamCopy.currentNodeIndex >= historyMoves.size
        if (isPastLastNode) {
            /* As the last node is the game termination (which belong to main variation) for history mode,
                or the last move number of main variation for game mode,
               we're sure that we'll find the end of main variation.
             */
            val previousNode = findPreviousMoveNode(
                historyMoves = historyMoves,
                nodeData = NodeSearchParam(
                    index = innerNextNodeSearchParamCopy.currentNodeIndex,
                    variationLevel = innerNextNodeSearchParamCopy.currentVariationLevel,
                ),
                selectedNodeVariationLevel = selectedNodeVariationLevel,
            )
            innerNextNodeSearchParamCopy = innerNextNodeSearchParamCopy.copy(
                currentNodeIndex = previousNode.index,
                currentVariationLevel = previousNode.variationLevel,
                hasJustMetCloseParenthesis = previousNode.hasJustMetCloseParenthesis,
                isLastMoveNodeOfMainVariation = true,
                mustBreakLoop = true,
            )
            /* updateInnerNodeSearchParamForCurrentLevelVariationsSearch has always been
              called for current node : we can break safely.
             */
            break
        }
        innerNextNodeSearchParamCopy = updateInnerNodeSearchParamForCurrentLevelVariationsSearch(
            historyMoves = historyMoves,
            innerNextNodeSearchParam = innerNextNodeSearchParamCopy,
            selectedNodeVariationLevel = selectedNodeVariationLevel,
        )
        if (innerNextNodeSearchParamCopy.mustBreakLoop) break
    }

    return innerNextNodeSearchParamCopy
}

fun findPreviousMoveNode(
    historyMoves: List<MovesNavigatorElement>,
    nodeData: NodeSearchParam,
    selectedNodeVariationLevel: Int,
): NodeSearchResult {
    var innerPreviousNodeSearchParam = InnerPreviousNodeSearchParam(
        currentNodeIndex = nodeData.index,
        currentVariationLevel = nodeData.variationLevel,
        previousTextIsOpenParenthesis = false,
        previousTextIsEndParenthesis = false,
        skippingSiblingVariation = false,
        needingToSkipOneMove = false,
        mustBreakLoop = false,
        hasJustMetOpenParenthesis = false,
        hasJustMetCloseParenthesis = false,
        hasJustMetMoveNumberAndOpenParenthesis = false,
        isLastMoveNodeOfMainVariation = false,
    )

    innerPreviousNodeSearchParam = loopSearchingForPreviousMoveNode(
        historyMoves = historyMoves,
        innerPreviousNodeSearchParam = innerPreviousNodeSearchParam,
        selectedNodeVariationLevel = selectedNodeVariationLevel,
    )

    return NodeSearchResult(
        index = innerPreviousNodeSearchParam.currentNodeIndex,
        variationLevel = innerPreviousNodeSearchParam.currentVariationLevel,
        mainVariationMoveText = null,
        hasJustMetOpenParenthesis = innerPreviousNodeSearchParam.hasJustMetOpenParenthesis,
        hasJustMetMoveNumberAndOpenParenthesis = innerPreviousNodeSearchParam.hasJustMetMoveNumberAndOpenParenthesis,
        hasJustMetCloseParenthesis = innerPreviousNodeSearchParam.previousTextIsEndParenthesis,
        variationsToProcess = listOf(),
        isLastMoveNodeOfMainVariation = innerPreviousNodeSearchParam.isLastMoveNodeOfMainVariation,
    )
}

fun findNextMoveNode(
    historyMoves: List<MovesNavigatorElement>,
    nodeData: NodeSearchParam,
    selectedNodeVariationLevel: Int,
): NodeSearchResult {
    var innerNodeSearchParam = InnerNextNodeSearchParam(
        currentNodeIndex = nodeData.index,
        currentVariationLevel = nodeData.variationLevel,
        hasJustMetCloseParenthesis = false,
        hasJustMetOpenParenthesis = false,
        hasJustMetCorrectLevelMoveNumberAndOpenParenthesis = false,
        mustBreakLoop = false,
        mainVariationMoveText = null,
        variationsToProcess = mutableListOf(),
        isLastMoveNodeOfMainVariation = false,
    )

    innerNodeSearchParam = loopSearchingForNextMoveNode(
        historyMoves = historyMoves,
        innerNextNodeSearchParam = innerNodeSearchParam,
        selectedNodeVariationLevel = selectedNodeVariationLevel,
    )

    val currentNode = historyMoves[innerNodeSearchParam.currentNodeIndex]
    innerNodeSearchParam.mainVariationMoveText = currentNode.text

    // As the called function modifies the node pointer, we have to use a copy instead.
    val variationsInnerNodeSearchParam = searchForCurrentLevelVariations(
        innerNextNodeSearchParam = innerNodeSearchParam,
        historyMoves = historyMoves,
        selectedNodeVariationLevel = selectedNodeVariationLevel,
    )

    return NodeSearchResult(
        index = innerNodeSearchParam.currentNodeIndex,
        variationLevel = innerNodeSearchParam.currentVariationLevel,
        mainVariationMoveText = innerNodeSearchParam.mainVariationMoveText,
        hasJustMetOpenParenthesis = innerNodeSearchParam.hasJustMetOpenParenthesis,
        hasJustMetMoveNumberAndOpenParenthesis = innerNodeSearchParam.hasJustMetCorrectLevelMoveNumberAndOpenParenthesis,
        hasJustMetCloseParenthesis = innerNodeSearchParam.hasJustMetCloseParenthesis,
        variationsToProcess = variationsInnerNodeSearchParam.variationsToProcess,
        isLastMoveNodeOfMainVariation = innerNodeSearchParam.isLastMoveNodeOfMainVariation,
    )
}

fun loopSearchingForPreviousMoveNode(
    historyMoves: List<MovesNavigatorElement>,
    innerPreviousNodeSearchParam: InnerPreviousNodeSearchParam,
    selectedNodeVariationLevel: Int,
): InnerPreviousNodeSearchParam {
    var innerPreviousNodeSearchParamCopy = innerPreviousNodeSearchParam.copy()
    val lastNodeIndex = historyMoves.size.dec()

    while (true) {
        innerPreviousNodeSearchParamCopy.currentNodeIndex--

        val isAtHistoryBeginning = innerPreviousNodeSearchParamCopy.currentNodeIndex < 0
        if (isAtHistoryBeginning) {
            val searchResult = NodeSearchResult(
                index = -1,
                variationLevel = 0,
                mainVariationMoveText = null,
                hasJustMetOpenParenthesis = false,
                hasJustMetMoveNumberAndOpenParenthesis = false,
                hasJustMetCloseParenthesis = false,
                variationsToProcess = listOf(),
                isLastMoveNodeOfMainVariation = innerPreviousNodeSearchParamCopy.isLastMoveNodeOfMainVariation,
            )
            innerPreviousNodeSearchParamCopy = innerPreviousNodeSearchParamCopy.copy(
                currentNodeIndex = searchResult.index,
                currentVariationLevel = searchResult.variationLevel,
                mustBreakLoop = true,
            )
        } else {
            val isPastLastMove =
                innerPreviousNodeSearchParamCopy.currentNodeIndex > lastNodeIndex
            if (isPastLastMove) {
                innerPreviousNodeSearchParamCopy.currentNodeIndex = lastNodeIndex
            }

            innerPreviousNodeSearchParamCopy = updateInnerNodeSearchParamForPreviousNodeSearch(
                historyMoves = historyMoves,
                innerPreviousNodeSearchParam = innerPreviousNodeSearchParamCopy,
                selectedNodeVariationLevel = selectedNodeVariationLevel,
            )
        }
        if (innerPreviousNodeSearchParamCopy.mustBreakLoop) break
    }
    return innerPreviousNodeSearchParamCopy
}

fun loopSearchingForNextMoveNode(
    historyMoves: List<MovesNavigatorElement>,
    innerNextNodeSearchParam: InnerNextNodeSearchParam,
    selectedNodeVariationLevel: Int,
): InnerNextNodeSearchParam {
    var innerNextNodeSearchParamCopy = innerNextNodeSearchParam.copy()

    val lastNodeIndex = historyMoves.size.dec()
    while (true) {
        innerNextNodeSearchParamCopy.currentNodeIndex++

        val isAtLeastInLastMove = innerNextNodeSearchParamCopy.currentNodeIndex >= lastNodeIndex
        if (isAtLeastInLastMove) {
            /* As the last node is the game termination (which belong to main variation),
               we're sure that we'll find the end of main variation.
             */
            innerNextNodeSearchParamCopy.currentNodeIndex = lastNodeIndex
            val previousNode = findPreviousMoveNode(
                historyMoves = historyMoves,
                nodeData = NodeSearchParam(
                    index = innerNextNodeSearchParamCopy.currentNodeIndex + 1,
                    variationLevel = innerNextNodeSearchParamCopy.currentVariationLevel
                ),
                selectedNodeVariationLevel = selectedNodeVariationLevel,
            )
            innerNextNodeSearchParamCopy = innerNextNodeSearchParamCopy.copy(
                currentNodeIndex = previousNode.index,
                currentVariationLevel = previousNode.variationLevel,
                hasJustMetCloseParenthesis = previousNode.hasJustMetCloseParenthesis,
                isLastMoveNodeOfMainVariation = true,
                mustBreakLoop = true,
            )
        } else {
            innerNextNodeSearchParamCopy = updateInnerNodeSearchParamForNextNodeSearch(
                innerNextNodeSearchParam = innerNextNodeSearchParamCopy,
                historyMoves = historyMoves,
                selectedNodeVariationLevel = selectedNodeVariationLevel,
            )
        }
        if (innerNextNodeSearchParamCopy.mustBreakLoop) break
    }

    return innerNextNodeSearchParamCopy
}

fun updateInnerNodeSearchParamForPreviousNodeSearch(
    historyMoves: List<MovesNavigatorElement>,
    innerPreviousNodeSearchParam: InnerPreviousNodeSearchParam,
    selectedNodeVariationLevel: Int,
): InnerPreviousNodeSearchParam {
    val innerPreviousNodeSearchParamCopy = innerPreviousNodeSearchParam.copy(
        mustBreakLoop = false,
    )

    val currentNode = historyMoves[innerPreviousNodeSearchParamCopy.currentNodeIndex]

    val sameLevelAsWhenStarting =
        innerPreviousNodeSearchParamCopy.currentVariationLevel == selectedNodeVariationLevel

    when {
        currentNode.text == "(" -> {
            innerPreviousNodeSearchParamCopy.hasJustMetOpenParenthesis = true
            if (innerPreviousNodeSearchParamCopy.skippingSiblingVariation) {
                innerPreviousNodeSearchParamCopy.skippingSiblingVariation = false
            }
            innerPreviousNodeSearchParamCopy.currentVariationLevel--
            innerPreviousNodeSearchParamCopy.previousTextIsOpenParenthesis = true
            innerPreviousNodeSearchParamCopy.previousTextIsEndParenthesis = false
            innerPreviousNodeSearchParamCopy.hasJustMetMoveNumberAndOpenParenthesis = false

            val upperLevelThanWhenStarting =
                innerPreviousNodeSearchParamCopy.currentVariationLevel < selectedNodeVariationLevel
            if (upperLevelThanWhenStarting) {
                innerPreviousNodeSearchParamCopy.needingToSkipOneMove = true
            }
        }
        currentNode.text == ")" -> {
            // We must be careful about siblings variations
            if (innerPreviousNodeSearchParamCopy.previousTextIsOpenParenthesis) {
                innerPreviousNodeSearchParamCopy.skippingSiblingVariation = true
            }
            innerPreviousNodeSearchParamCopy.hasJustMetCloseParenthesis = true
            innerPreviousNodeSearchParamCopy.currentVariationLevel++
            innerPreviousNodeSearchParamCopy.previousTextIsOpenParenthesis = false
            innerPreviousNodeSearchParamCopy.previousTextIsEndParenthesis = true
            innerPreviousNodeSearchParamCopy.hasJustMetMoveNumberAndOpenParenthesis = false
        }
        currentNode.fen != null -> {
            if (innerPreviousNodeSearchParamCopy.needingToSkipOneMove) {
                innerPreviousNodeSearchParamCopy.needingToSkipOneMove = false
                innerPreviousNodeSearchParamCopy.previousTextIsOpenParenthesis = false
                innerPreviousNodeSearchParamCopy.previousTextIsEndParenthesis = false
            } else {
                val upperLevelThanWhenStarting =
                    innerPreviousNodeSearchParamCopy.currentVariationLevel < selectedNodeVariationLevel
                if (sameLevelAsWhenStarting && !innerPreviousNodeSearchParamCopy.skippingSiblingVariation) {
                    innerPreviousNodeSearchParamCopy.mustBreakLoop = true
                } else if (upperLevelThanWhenStarting && !innerPreviousNodeSearchParamCopy.needingToSkipOneMove) {
                    innerPreviousNodeSearchParamCopy.mustBreakLoop = true
                }
            }
            innerPreviousNodeSearchParamCopy.previousTextIsOpenParenthesis = false
            innerPreviousNodeSearchParamCopy.previousTextIsEndParenthesis = false
            innerPreviousNodeSearchParamCopy.hasJustMetMoveNumberAndOpenParenthesis = false
        }
        else -> {
            innerPreviousNodeSearchParamCopy.hasJustMetMoveNumberAndOpenParenthesis =
                currentNode.text.isMoveNumber() && innerPreviousNodeSearchParamCopy.hasJustMetOpenParenthesis
            innerPreviousNodeSearchParamCopy.previousTextIsOpenParenthesis = false
            innerPreviousNodeSearchParamCopy.previousTextIsEndParenthesis = false
        }
    }

    return innerPreviousNodeSearchParamCopy
}

private fun String.isMoveNumber(): Boolean {
    val interestingPart =
        this.substring(
            0,
            this.length - if (this.endsWith("...")) 3 else 1
        )
    return try {
        Integer.parseInt(interestingPart)
        true
    } catch (ex: NumberFormatException) {
        false
    }
}
