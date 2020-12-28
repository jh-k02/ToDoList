package com.example.todolist

import android.app.Application
import io.realm.Realm

class MyApplication : Application(){ // Application 클래스를 상속 받는 MyApplication 클래스를 선언.
    override fun onCreate() { // onCreate 메서드를 오버라이드.
        super.onCreate()
        Realm.init(this) // Realm.init 메서드를 사용하여 초기화.
    }
}