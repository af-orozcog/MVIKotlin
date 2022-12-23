package com.arkivanov.mvikotlin.plugin.idea.timetravel

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction.TimeTravelFunction
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelparametersignature.TimeTravelParameterSignature
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.swing.AbstractCellEditor
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer


class PastEventsView() {

    var panel = JPanel()
    val component: JComponent get() = panel
    val gbc = GridBagConstraints()

    init {
        panel.layout = GridBagLayout()
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridx = 0
    }

    fun addRow(toAdd:JBScrollPane){
        panel.add(toAdd,gbc)
        panel.updateUI()
    }

    fun removeAllComponents(){
        var components = panel.components
        for(component in components){
            panel.remove(component)
        }
        panel.updateUI()
    }

    /*
    lateinit var table: JBTable
    lateinit private var eventsCell:EventsCell

    val component: JComponent get() = table

    internal class EventsFeedTableModel(var events: List<List<String>>) : AbstractTableModel() {
        override fun getColumnClass(columnIndex: Int): Class<*> {
            return List::class.java
        }

        override fun getColumnCount(): Int {
            return 1
        }

        override fun getColumnName(columnIndex: Int): String {
            return "Past Events"
        }

        override fun getRowCount(): Int {
            return events.size
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            return events[rowIndex]
        }

        override fun isCellEditable(columnIndex: Int, rowIndex: Int): Boolean {
            return true
        }
    }

    internal class EventsCell(var numberRows:Int, private val listener: TimeTravelView.Listener): AbstractCellEditor(), TableCellEditor, TableCellRenderer {
        private val listModel = DefaultListModel<String>()
        private val list = JBList(listModel).apply{
            this.layoutOrientation = JBList.HORIZONTAL_WRAP
        }
        private var event:List<String> = emptyList()
        private val panel = JBScrollPane(list)
        private var selectionListener: ListSelectionListener? = null


        private fun updateData(event: List<String>, isSelected: Boolean, table: JTable,row: Int) {
            this.event = event
            //if(selectionListener != null) {
            //    list.removeListSelectionListener(selectionListener)
            //}
            listModel.clear()
            event.forEach(listModel::addElement)
            //selectionListener = customListener(numberRows-1-row, listener)
            //list.addListSelectionListener(selectionListener)
            list.updateUI()
        }

        override fun getTableCellEditorComponent(
            table: JTable, value: Any,
            isSelected: Boolean, row: Int, column: Int
        ): Component {
            updateData(value as List<String>, true, table,row)
            showErrorDialog(text = "editor se llamo")
            return panel
        }

        override fun getCellEditorValue(): Any {
            showErrorDialog(text = "el editor value se llamo")
            return event
        }

        override fun getTableCellRendererComponent(
            table: JTable, value: Any,
            isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
        ): Component {
            updateData(value as List<String>, true, table,row)
            showErrorDialog(text = "renderer se llamo")
            return panel
        }

        internal class customListener(private var row:Int,val listener: TimeTravelView.Listener):
            ListSelectionListener {
            override fun valueChanged(p0: ListSelectionEvent?) {
                if(p0 == null) return
                if(p0.firstIndex!! == -1) return
                listener.onEventSelected(row,p0.firstIndex)
            }

        }
    }

    init{
        events = events.reversed()
        table = JBTable(EventsFeedTableModel(events))
        eventsCell = EventsCell(events.size,listener)
        table.setDefaultRenderer(List::class.java, eventsCell)
        table.setDefaultEditor(List::class.java, eventsCell)
    }

    fun updateTable(events:List<List<String>>){
        this.events = events.reversed()
        table.model = EventsFeedTableModel(this.events)
        eventsCell.numberRows = events.size
        table.updateUI()
    }
    */
}
