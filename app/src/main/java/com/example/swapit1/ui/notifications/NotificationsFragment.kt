package com.example.swapit1.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swapit1.R
import com.example.swapit1.adapter.NotificationAdapter
import com.example.swapit1.databinding.FragmentNotificationsBinding
import com.example.swapit1.model.Notification

class NotificationsFragment : Fragment() {

    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // اربطي بالتصميم
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_notifications)
        val toolbar = view.findViewById<Toolbar>(R.id.myToolbar)


        val notifications = listOf(
            Notification(R.drawable.key, "تم تسجيل الدخول بنجاح، مرحبًا بك من جديد!", "منذ ساعة"),
            Notification(R.drawable.offer, "تم نشر عرضك بنجاح! سيتم إعلامك عند وصول الطلبات.", "منذ 5 دقائق"),
            Notification(R.drawable.emailreq, "تم إرسال طلبك، سيتم إشعارك عند الرد.", "منذ 10 دقائق"),
            Notification(R.drawable.accept3, "تم قبول طلبك! تواصل مع الطرف الآخر الآن.", "منذ ساعتين"),
            Notification(R.drawable.close, "نأسف، تم رفض طلبك من قبل الطرف الآخر.", "منذ ساعتين"),
            Notification(R.drawable.eyes, "شخص مهتم بعرضك! تحقق من الطلبات الجديدة.", "منذ ساعتين")


        )


        val adapter = NotificationAdapter(notifications)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
 return view
    }



}