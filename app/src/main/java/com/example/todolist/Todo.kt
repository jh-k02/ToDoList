package com.example.todolist

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Todo ( // 코틀린에서는 Realm에서 사용하는 클래스에 open 키워드를 사용.
    @PrimaryKey var id: Long = 0, // id는 유일한 값이 되어야 하기 때문에 PrimaryKey 제약을 주석으로 추가. DB에서는 데이터를 식별하는 유일한 키 값을 기본키라고 함. 기본키 제약은 Realm에서 제공.
    var title: String = "",     // 할 일 제목
    var date: Long = 0,         // 마감일
    var category: String = "",  // 카테고리 이름
    var check: Boolean = false  // 완성 여부
) : RealmObject(){ // RealmObject를 상속 받아 Realm DB를 다룰 수 있음.

}