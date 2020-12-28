package com.example.todolist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.item_todo.*
import kotlinx.android.synthetic.main.item_todo.view.*
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {

    val realm = Realm.getDefaultInstance()  // Realm 객체를 초기화

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val categoryList = searchCategory()
            // 할 일 추가
            startActivity<EditActivity>("categoryList" to categoryList)
        }

        menu.setOnClickListener {
            // 리스트뷰에 항목이 없는 경우 화면전환 X
            if(listView.count == 0) {
                alert("항목을 추가해주세요.", "항목이 없습니다.") {
                    yesButton { }
                }.show()
            }

            // 리스트뷰에 항목이 있을 경우에만 화면전환
            else {
                val items = listOf("오늘 할 일", "이번주 할 일", "카테고리 보기", "달성 목록보기", "미달성 목록보기")
                selector(title = "메뉴 선택", items = items) { dig, selection ->
                    if (selection == 0)
                        startActivity<MenuActivity>("selection" to items[selection])
                    else if (selection == 1)
                        startActivity<MenuActivity>("selection" to items[selection])
                    else if (selection == 2) {
                        val categoryList = searchCategory()
                        selector(title = "카테고리 선택", items = categoryList) { dig, sel ->
                            startActivity<MenuActivity>("selection" to items[selection], "categoryList" to categoryList[sel])
                        }
                    }
                    else if (selection == 3)
                        startActivity<MenuActivity>("selection" to items[selection])
                    else if (selection == 4)
                        startActivity<MenuActivity>("selection" to items[selection])
                }
            }
        }

        // 전체 할 일 정보를 가져와서 날짜 순으로 내림차순 정렬
        val realmResult = realm.where<Todo>().findAll().sort("date", Sort.DESCENDING) // sort(findName: String, sortOrder: Sort), 오름차순 = ASCENDING
        val adapter = TodoAdapter(realmResult)

        listView.adapter = adapter
        searchTodo()

        // 데이터가 변경되면 어댑터에 적용.
        realmResult.addChangeListener { _ ->
            adapter.notifyDataSetChanged()
            searchTodo()
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val categoryList = searchCategory()
            // 할 일 수정
            startActivity<EditActivity>("id" to id, "categoryList" to categoryList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close() //Realm 객체 해제
    }

    // 할 일 달성도
    private fun searchTodo() {
        val maxId = realm.where<Todo>().max("id")

        var count = 0
        if (listView.count != 0) {
            realm.beginTransaction()

            for (id in 0..maxId!!.toInt()) {
                if (realm.where<Todo>().equalTo("id", id).findFirst() != null) {
                    val searchItem = realm.where<Todo>().equalTo("id", id).findFirst()!!
                    if (searchItem.check)
                        count++
                }
            }

            realm.commitTransaction()
            val per = count.toDouble() / listView.count * 100
            percent.text = String.format("%.1f", per)
            progressBar.setProgress(per.toInt())
        }

        else {
            percent.text = "100.0"
            progressBar.setProgress(100)
        }
    }

    // 카테고리 리스트 생성
    private fun searchCategory(): ArrayList<String> {
        val realmResult = realm.where<Todo>().findAll().sort("category", Sort.ASCENDING)    // 오른차순 정렬
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
