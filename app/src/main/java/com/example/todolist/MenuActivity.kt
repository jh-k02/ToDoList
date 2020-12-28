package com.example.todolist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.content_menu.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton
import java.text.SimpleDateFormat
import java.util.*

class MenuActivity : AppCompatActivity() {

    val realm = Realm.getDefaultInstance()  // Realm 객체를 초기화

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val selection: String = intent.getStringExtra("selection")
        var result: ArrayList<Todo>? = null

        menu_name.text = selection

        var realmResult = realm.where<Todo>().findAll().sort("date", Sort.DESCENDING) // 내림차순 정렬

        var strDate = ""
        var flag = 0
        var menu_flag = true

        // 오늘 할 일 메뉴 선택에 대한 동작
        if(selection == "오늘 할 일") {
            flag = 1
            date_flag.text = "1"
            strDate = SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREA).format(Date())
        }

        // 이번주 할 일 메뉴 선택에 대한 동작
        else if(selection == "이번주 할 일") {
            flag = 2
            date_flag.text = "2"
            strDate = SimpleDateFormat("yyyy년 MM월 W주차", Locale.KOREA).format(Date())
        }

        // 카테고리 보기 메뉴 선택에 대한 동작
        else if(selection == "카테고리 보기") {
            flag = 3
            date_flag.text = "3"
            strDate = intent.getStringExtra("categoryList")
        }

        // 달성 목록보기 메뉴 선택에 대한 동작
        else if(selection == "달성 목록보기") {
            flag = 4
            date_flag.text = "4"
            strDate = "true"
        }

        // 미달성 목록보기 메뉴 선택에 대한 동작
        else {
            flag = 5
            date_flag.text = "5"
            strDate = "false"
        }

        Day.text = strDate

        // 달성/미달성 목록보기 메뉴일 경우 Day의 텍스트 공백
        if(flag == 4 || flag == 5) {
            Day.text = ""
        }

        result = dayList(strDate, flag, realmResult)

        // 선택 메뉴에 대한 목록이 없으면 액티비티 종료
        if (result.size == 0) {
            alert("항목이 없습니다.") {
                yesButton { finish() }
            }.show()
        } else {        // 목록이 있는 경우에만 동작 실행
            val adapter = MyAdapter(this, result)
            listView_menu.adapter = adapter
            list_num.text = listView_menu.count.toString()
            searchTodo(strDate, flag)

            // 데이터가 변경되면 어댑터에 적용.
            realmResult.addChangeListener { _ ->
                if (menu_flag)
                    searchTodo(strDate, flag)
            }

            listView_menu.setOnItemClickListener { parent, view, position, id ->
                val categoryList = searchCategory()
                menu_flag = false
                finish()
                // 할 일 수정
                startActivity<EditActivity>("id" to id, "categoryList" to categoryList)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close() //Realm 객체 해제
    }

    // 할 일 달성도
    private fun searchTodo(strDate: String, flag: Int) {
        val maxId = realm.where<Todo>().max("id")
        realm.beginTransaction()
        var count = 0

        if(listView_menu.count != 0) {
            for (id in 0..maxId!!.toInt()) {
                if (realm.where<Todo>().equalTo("id", id).findFirst() != null) {
                    val searchItem = realm.where<Todo>().equalTo("id", id).findFirst()!!
                    var date: CharSequence? = null
                    if (flag == 1) {
                        date = DateFormat.format("yyyy년 MM월 dd일 E요일", searchItem.date)
                    } else if (flag == 2) {
                        date = SimpleDateFormat("yyyy년 MM월 W주차", Locale.KOREA).format(searchItem.date)
                    } else if (flag == 3) {
                        date = searchItem.category
                    } else if (flag == 4) {
                        date = searchItem.check.toString()
                    }

                    if (date == strDate && searchItem.check)
                        count++
                }
            }
            val per = count.toDouble() / listView_menu.count * 100
            menu_percent.text = String.format("%.1f", per)
            menu_progressBar.setProgress(per.toInt())

            realm.commitTransaction()
        }

        else {
            menu_percent.text = "100.0"
            menu_progressBar.setProgress(100)
        }
    }

    // 리스트 어댑터에 해당 날짜 또는 주차의 할 일 목록 출력
    // flag ==> 1: 오늘 할 일, 2: 이번주 할 일, 3: 카테고리 보기, 4: 미달성 목록보기
    fun dayList(strDate: String, flag: Int, realmResult: RealmResults<Todo>): ArrayList<Todo> {
        realm.beginTransaction()

        var result: ArrayList<Todo> = ArrayList()
        for (id in 0..realmResult.size - 1) {
            if(realmResult[id] != null) {
                val searchItem = realmResult[id]!!
                var date: CharSequence? = null
                if (flag == 1) {
                    date = DateFormat.format("yyyy년 MM월 dd일 E요일", searchItem.date)
                } else if (flag == 2) {
                    date = SimpleDateFormat("yyyy년 MM월 W주차", Locale.KOREA).format(searchItem.date)
                } else if (flag == 3) {
                    date = searchItem.category
                } else if (flag == 4 || flag == 5) {
                    date = searchItem.check.toString()
                }

                if (date == strDate)
                    result!!.add(searchItem)
            }
        }


        realm.commitTransaction()

        return result
    }

    // 카테고리 리스트 생성
    private fun searchCategory(): ArrayList<String> {
        val realmResult = realm.where<Todo>().findAll().sort("category", Sort.ASCENDING)    // 오름차순 정렬
        var categoryList: ArrayList<String> = ArrayList()
        realm.beginTransaction()

        if(realmResult != null) {
            for(id in 0..realmResult.size - 1) {
                var equal_check = true
                for(i in 0..categoryList.size - 1) {
                    if(realmResult[id]!!.category == categoryList[i]) {
                        equal_check = false
                        break
                    }
                }
                if(equal_check) {
                    categoryList.add(realmResult[id]!!.category)
                }
            }
        }

        realm.commitTransaction()
        return categoryList
    }
}