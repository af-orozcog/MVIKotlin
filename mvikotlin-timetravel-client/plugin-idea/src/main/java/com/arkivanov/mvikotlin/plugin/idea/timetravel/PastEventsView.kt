package com.arkivanov.mvikotlin.plugin.idea.timetravel

import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.Component
import javax.swing.AbstractCellEditor
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer


class PastEventsView(private var events: List<List<String>>) {

    lateinit var table: JBTable

    val component: JComponent get() = table

    class EventsFeedTableModel(private var events: List<List<String>>) : AbstractTableModel() {
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

    class EventsCell : AbstractCellEditor(), TableCellEditor, TableCellRenderer {
        private val listModel = DefaultListModel<String>()
        private val list = JBList(listModel).apply{
            this.layoutOrientation = JBList.HORIZONTAL_WRAP
        }
        private var event:List<String> = emptyList()
        private val panel = JBScrollPane(list)



        private fun updateData(event: List<String>, isSelected: Boolean, table: JTable) {
            this.event = event
            listModel.clear()
            event.forEach(listModel::addElement)
            list.updateUI()
        }

        override fun getTableCellEditorComponent(
            table: JTable, value: Any,
            isSelected: Boolean, row: Int, column: Int
        ): Component {
            updateData(value as List<String>, true, table)
            return panel
        }

        override fun getCellEditorValue(): Any {
            return 1
        }

        override fun getTableCellRendererComponent(
            table: JTable, value: Any,
            isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
        ): Component {
            updateData(value as List<String>, true, table)
            return panel
        }
    }

    init{
        events = events.reversed()
        table = JBTable(EventsFeedTableModel(events))
        table.setDefaultRenderer(List::class.java, EventsCell())
        table.setDefaultEditor(List::class.java, EventsCell())
    }

    fun updateTable(events:List<List<String>>){
        this.events = events.reversed()
        table.model = EventsFeedTableModel(this.events)
        table.updateUI()
    }

}
