package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain

// Дополнительные действия с заявкой
enum class EditorAction(val navId: Int) {
    AddDoer(0),
    AddResource(0),
    SelectTime(0),
    ConfirmForDoer(0),
    WaitForDoer(0),
    RefuseForDoer(0),
    RefuseAsDoer(0),
    ConfirmAsDoer(0),
    DragToLeft(0),
    DragToRight(0),
    SwipeToRightStarted(0),
    SwipeToLeftStarted(0),
    SwipeToRightEnded(0),
    SwipeToLeftEnded(0),
    SwipeToCenterEnded(0),
    SwipeToAbove(0),
    SwipeToBelow(0),
    AddItem(0),
    DeleteItem(0),
    DeleteDoer(0),
    DeleteResource(0),
    DeleteStuff(0),
    ChangeDoerState(0),
    HighlightButtons(0),
    ConfirmChanges(0),
    EditTime(0),
    UndoChanges(0)
}