package com.arkivanov.mvikotlin.plugin.idea.timetravel

import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.arkivanov.mvikotlin.timetravel.client.internal.client.TimeTravelClient.Model
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction.TimeTravelFunction
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelparametersignature.TimeTravelParameterSignature
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.value.ValueNode
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import org.jdesktop.swingx.renderer.DefaultListRenderer
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode

class TimeTravelView(
    private val listener: Listener
) {

    private val toolbar = TimeTravelToolbar(listener)

    private val listModel = DefaultListModel<String>()

    private val list =
        JBList(listModel).apply {
            addListSelectionListener {
                listener.onEventSelected(index = selectedIndex)
            }
        }

    private var selectionListener: ListSelectionListener? = null

    private val functionsModel = DefaultListModel<String>()
    private val functionsList = JBList(functionsModel)

    private val treeModel = DefaultTreeModel(null)
    private val tree = JTree(treeModel)

    private val centerPart = JBSplitter(false, SPLITTER_PROPORTION).apply {
        firstComponent = JBScrollPane(list)
        secondComponent = JBScrollPane(tree)
    }

    val content: JComponent =
        JPanel(BorderLayout()).apply {
            add(toolbar.component, BorderLayout.NORTH)

            add(
                JBSplitter(true, SPLITTER_PROPORTION).apply {
                    firstComponent = centerPart
                    secondComponent = JBScrollPane(functionsList)
                },
                BorderLayout.CENTER
            )
        }

    private val renderer: ViewRenderer<Model> =
        diff {
            diff(get = Model::events, set = ::renderEvents)
            diff(get = Model::currentEventIndex, set = ::renderCurrentEventIndex)
            diff(get = Model::buttons, set = toolbar::render)
            diff(get = Model::selectedEventIndex, set = ::renderSelectedEventIndex)
            diff(get = Model::selectedEventValue, set = ::renderSelectedEventValue)
            diff(get = Model::errorText, set = ::renderError)
            diff(get = Model::exposedFunctions, set = ::renderExposedFunctions)
        }

    fun render(model: Model) {
        renderer.render(model)
    }

    private fun renderEvents(events: List<List<String>>) {
        val selectedIndex = list.selectedIndex
        listModel.clear()
        if(events.size != 0){
            events[events.size-1].forEach(listModel::addElement)
        }
        list.selectedIndex = selectedIndex
        list.updateUI()
    }

    private fun renderExposedFunctions(functions: List<TimeTravelFunction>) {
        if(selectionListener != null){
            functionsList.removeListSelectionListener(selectionListener)
        }
        functionsModel.clear()
        functions.forEach{
            function -> functionsModel.addElement(function.name + ": "+ function.type)
        }

        functionsList.updateUI()
        selectionListener = customListener(functions,listener,functionsList)
        functionsList.addListSelectionListener(selectionListener)

    }

    private fun renderCurrentEventIndex(selectedEventIndex: Int) {
        list.cellRenderer =
            DefaultListRenderer(
                TimeTravelEventComponentProvider(
                    font = list.font,
                    selectedEventIndex = selectedEventIndex
                )
            )
    }

    private fun renderSelectedEventIndex(index: Int) {
        list.selectedIndex = index
    }

    private fun renderSelectedEventValue(value: ValueNode?) {
        treeModel.setRoot(value?.toTreeNode())
    }

    private fun ValueNode.toTreeNode(): MutableTreeNode =
        DefaultMutableTreeNode(title).apply {
            children.forEach {
                add(it.toTreeNode())
            }
        }

    private fun renderError(text: String?) {
        if (text != null) {
            showErrorDialog(text = text)
        }
    }

    private companion object {
        private const val SPLITTER_PROPORTION = 0.4F
    }

    class customListener(private val functions: List<TimeTravelFunction>,private val listener: Listener,private val functionsList: JBList<String>):ListSelectionListener{
        override fun valueChanged(p0: ListSelectionEvent?) {
            if(p0 == null) return
            if(functions.size <= p0?.firstIndex!!) return
            var toSend = createFunctionsParams(functions[p0?.firstIndex!!].parameters)
            listener.onApplyFunction(functions[p0?.firstIndex!!].name,toSend)
            functionsList.removeListSelectionListener(this)
            functionsList.selectedIndex = -1
            functionsList.updateUI()
        }

        private fun createFunctionsParams(parameters: List<TimeTravelParameterSignature>):List<Pair<String,Any>>{
            var answer:List<Pair<String,Any>> = emptyList()
            for(signature in parameters){
                val toAdd = JOptionPane.showInputDialog(null, "name: "+signature.name+",type: "+signature.type, "Parameter", JOptionPane.QUESTION_MESSAGE);
                answer = answer + Pair(signature.type,toAdd as Any)
            }

            return answer
        }
    }

    interface Listener : TimeTravelToolbar.Listener {
        fun onEventSelected(index: Int)
        fun onApplyFunction(functionName:String,arguments:List<Pair<String,Any>>)
    }
}
