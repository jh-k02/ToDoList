package com.example.todolist

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import io.realm.Realm
import io.realm.kotlin.where

class MyAdapter (val context: Context, val itemList: ArrayList<Todo>): BaseAdapter() {
    val realm = Realm.getDefaultInstance()  // Realm 객체를 초기화
    var b_check = false

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_todo, null)

        val dateTextView: TextView = view.findViewById(R.id.text_date)
        val textTextView: TextView = view.findViewById(R.id.text_work)
        val categoryTextView: TextView = view.findViewById(R.id.text_category)
        val check: CheckBox = view.findViewById(R.id.checkBox)
        val list_id: TextView = view.findViewById(R.id.list_id)

        val item = itemList[position]

        textTextView.text = item.title // 각각 값을 뷰에 표시.
        dateTextView.text = DateFormat.format("yy.MM.dd", item.date)
        categoryTextView.text = item.category
        check.isChecked = item.check
        list_id.text = item.id.toString()

        check.setOnClickListener {
            b_check = check.isChecked

            realm.beginTransaction()
            val maxId = realm.where<Todo>().max("id")

            for (id in 0..maxId!!.toInt()) {
                if (realm.where<Todo>().equalTo("id", id).findFirst() != null) {
                    val updateItem = realm.where<Todo>().equalTo("id", id).findFirst()!! // T타입 객체로 부터 데이터를 받는데 equalTo() 메서드로 조건을 설정. findFirst() 메서드로 첫 데이터를 반환. !!는 절대 null이 아님.

                    if (updateItem.id.toString() == list_id.text) {
                        updateItem.check = b_check
                        break
                    }
                }
            }

            realm.commitTransaction()
        }

        return view
    }

    override fun getItem(position: Int): Any {
        return itemList[position]
    }

    override fun getItemId(position: Int): Long {
        return itemList[position].id
    }

    override fun getCount(): Int {
        return itemList.size
    }
}