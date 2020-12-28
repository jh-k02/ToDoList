package com.example.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter
import android.text.format.DateFormat
import android.widget.CheckBox
import android.widget.TextView
import io.realm.Realm
import io.realm.kotlin.where

class TodoAdapter(realmResult: OrderedRealmCollection<Todo>): RealmBaseAdapter<Todo>(realmResult){
    var check = false
    val realm = Realm.getDefaultInstance()  // Realm 객체를 초기화

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val vh: ViewHolder
        val view: View

        if (convertView == null){ // convertView가  null이 되면 레이아웃을 작성.
            view = LayoutInflater.from(parent?.context).inflate(R.layout.item_todo, parent, false) // inflate(resource, root, attachToRoot(XML을 부를땐 항상 false))는 XML 코드를 뷰로 반환하는 기능을 함.

            vh = ViewHolder(view) // ViewHolder 객체 초기화.
            view.tag = vh //
        }
        else { // convertView 가 null이 아니면 이전에 사용한 View를 재사용한다.
            view = convertView

            vh = view.tag as ViewHolder // ViewHolder 객체를 tag 프로퍼티에서 꺼내고 반환형을 ViewHolder로 바꿔준다.
        }

        if (adapterData != null){ //RealmBaseAdapter는 adapterData 프로퍼티를 제공.
            val item = adapterData!![position] // 제공할 수 있는 값이 있다면 해당 위치 데이터를 item 변수에 저장.

            // 각각 값을 뷰에 표시.
            vh.textTextView.text = item.title
            vh.dateTextView.text = DateFormat.format("yy.MM.dd", item.date)
            vh.categoryTextView.text = item.category
            vh.check.isChecked = item.check
            vh.list_id.text = item.id.toString()

            // 체크박스가 클릭 되면 해당 realm id의 check 정보 수정
            vh.check.setOnClickListener {
                check = vh.check.isChecked

                realm.beginTransaction()
                val maxId = realm.where<Todo>().max("id")

                for (id in 0..maxId!!.toInt()) {
                    if (realm.where<Todo>().equalTo("id", id).findFirst() != null) {
                        val updateItem = realm.where<Todo>().equalTo("id", id).findFirst()!! // T타입 객체로 부터 데이터를 받는데 equalTo() 메서드로 조건을 설정. findFirst() 메서드로 첫 데이터를 반환. !!는 절대 null이 아님.

                        if (updateItem.id.toString() == vh.list_id.text) {
                            updateItem.check = check
                            break
                        }
                    }
                }

                realm.commitTransaction()

            }
        }

        return view // 완성된 view를 반환.
    }

    override fun getItemId(position: Int): Long { // 리스트뷰를 클릭하여 이벤트를 처리할 때 id 값을 이용하여 그것을 반환하도록 함.
        if (adapterData != null){
            return adapterData!![position].id
        }
        return super.getItemId(position)
    }

    inner class ViewHolder(view: View) {

        val dateTextView: TextView = view.findViewById(R.id.text_date)
        val textTextView: TextView = view.findViewById(R.id.text_work)
        val categoryTextView: TextView = view.findViewById(R.id.text_category)
        val check: CheckBox = view.findViewById(R.id.checkBox)
        val list_id: TextView = view.findViewById(R.id.list_id)
    }
}

