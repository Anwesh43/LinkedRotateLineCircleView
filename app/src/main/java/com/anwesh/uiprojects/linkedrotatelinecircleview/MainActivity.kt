package com.anwesh.uiprojects.linkedrotatelinecircleview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.rotatelinecircleview.RotateLineCircleView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RotateLineCircleView.create(this)
    }
}
