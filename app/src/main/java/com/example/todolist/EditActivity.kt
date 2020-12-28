package com.example.todolist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_edit.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.yesButton
import java.util.*

class EditActivity : AppCompatActivity() {
    val realm = Realm.getDefaultInstance() // 인스턴스 얻기
    val calendar: Calendar = Calendar.getInstance() // 날짜를 다룰 캘린더 객체
    var calendar_check = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // 업데이트 조건
        val id = intent.getLongExtra("id", -1L) //L은 데이터형이 Long이라는 것.
        if (id == -1L){
            insertMode()
        }
        else{
            updateMode(id)
        }

        // 캘린더 뷰의 날짜를 선택했을 때 Calendar 객체에 설정
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar_check = true
        }

        categoryBtn.setOnClickListener {
            val categoryList = intent.getStringArrayListExtra("categoryList")
            selector(title = "카테고리 선택", items = categoryList) { dig, selection ->
                todoEditCategory.setText(categoryList[selection])
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close() //  인스턴스 해제
    }

    // 추가 모드 초기화
    private fun insertMode() {
        // 삭제버튼 감추기
        deleteFab.hide()
        // 완료 버튼을 클릭하면 추가
        doneFab.setOnClickListener {
            insertTodo()
        }
    }

    // 수정 모드 초기화
    private fun updateMode(id: Long) {
        // id에 해당하는 객체를 화면에 표시
        val todo = realm.where<Todo>().equalTo("id", id).findFirst()!!
        todoEditText.setText(todo.title)
        calendarView.date = todo.date
        todoEditCategory.setText(todo.category)

        // 완료버튼을 클릭하면 수정
        doneFab.setOnClickListener {
            updateTodo(id)
        }

        // 삭제버튼을 클릭하면 삭제
        deleteFab.setOnClickListener {
            deleteTodo(id)
        }
    }

    // 할 일 추가
    private  fun insertTodo(){
        realm.beginTransaction() // 트랜잭션 시작, Realm에서 데이터를 변경 할 때 이 메서드로 트랜잭션을 실행한다.

        val newItem = realm.createObject<Todo>(nextId()) // 새 객체 생성 <>사이는 primayKeyValue로 기본키를 정해준다.

        // 입력한 정보를 객체에 추가
        newItem.title = todoEditText.text.toString()
        newItem.date = calendar.timeInMillis
        if(todoEditCategory.text.toString() == "")
            newItem.category = "카테고리 없음"
        else
            newItem.category = todoEditCategory.text.toString()

        realm.commitTransaction() // 트랜잭션 종료 반영, DB에서 트랜잭션을 시작했으면 반드시 닫아야 한다.

        // 다이얼로그 표시
        alert("내용이 추가되었습니다."){
            yesButton { finish() }
        }.show()
    }

    // 다음 ID를 반환
    private fun nextId(): Int {
        val maxId = realm.where<Todo>().max("id") // max() 메서드는 열 값 중에 가장 큰 값을 String으로 반환한다.
        if (maxId != null) {
            return maxId.toInt() + 1
        }
        return 0
    }

    // 할 일 수정
    private fun updateTodo(id: Long){
        realm.beginTransaction()    // 트랜잭션 시작

        val updateItem = realm.where<Todo>().equalTo("id", id).findFirst()!! // T타입 객체로 부터 데이터를 받는데 equalTo() 메서드로 조건을 설정. findFirst() 메서드로 첫 데이터를 반환. !!는 절대 null이 아님.

        // 입력한 정보를 객체에 저장(수정)
        updateItem.title = todoEditText.text.toString()
        if(calendar_check)
            updateItem.date = calendar.timeInMillis
        if(todoEditCategory.text.toString() == "")
            updateItem.category = "카테고리 없음"
        else
            updateItem.category = todoEditCategory.text.toString()

        // 다이얼로그 표시
        alert("내용이 수정되었습니다.") {
            yesButton {
                realm.commitTransaction()   // 트랜잭션 종료
                finish()
            }
        }.show()
    }

    // 할 일 삭제
    private fun deleteTodo(id: Long){
        realm.beginTransaction()    // 트랜잭션 시작

        val deleteItem = realm.where<Todo>().equalTo("id", id).findFirst()!!

        // 삭제할 객체
        deleteItem.deleteFromRealm()

        realm.commitTransaction()   // 트랜잭션 종료

        // 다이얼로그 표시
        alert("내용이 삭제되었습니다."){
            yesButton {
                finish()
            }
        }.show()
    }
}
