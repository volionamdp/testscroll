package com.example.testscroll

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.controller.data.ImageModel
import com.example.controller.view.ControllerView
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test()
        Glide.with(this).load("/storage/emulated/0/DCIM/Camera/20220508_161032.jpg").into(findViewById(R.id.imageTest))
    }
    private fun test(){
        val list: MutableList<String> = mutableListOf()
        list.add("/storage/emulated/0/DCIM/Camera/20220508_161032.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20210602_142205.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20210527_155844.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20220408_162138.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20220508_161014.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20210625_162116.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20220508_161017.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20220508_161024.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20220508_161021.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20220508_161019.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20220508_161026.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20220508_161029.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/ccc.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/aaa.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20201224_180458.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/20201216_170715.jpg")
        val view = findViewById<ControllerView>(R.id.controller)
        for ((index,item) in list.withIndex()){
            view.addImage(ImageModel(item,index*4000L,(index+1)*4000L))
        }
    }
}