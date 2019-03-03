package com.ai.deep.andy.carrecognizer.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ai.deep.andy.carrecognizer.LoginActivity
import com.ai.deep.andy.carrecognizer.MainActivity

import com.ai.deep.andy.carrecognizer.R
import com.ai.deep.andy.carrecognizer.model.User
import com.orm.SugarRecord
import kotlinx.android.synthetic.main.fragment_user.*
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UserFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_user, container, false)

        if (user == null){
            val users = SugarRecord.findAll(User::class.java)
            if(users.hasNext()){
                user = users.next()
            }
        }

        v.findViewById<TextView>(R.id.welcome_message).text = user?.first_name + " " + user?.last_name
        v.findViewById<TextView>(R.id.email_value).text = user?.email

        val calendar : Calendar = Calendar.getInstance()
        if(user?.registration_date != null) {
            calendar.timeInMillis = user?.registration_date!!

            v.findViewById<TextView>(R.id.registration_date_value).text = calendar.get(Calendar.YEAR).toString() +
                    "-" + calendar.get(Calendar.MONTH).toString() +
                    "-" +  calendar.get(Calendar.DAY_OF_MONTH).toString() +
                    " " + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE)
        }



        val logout: FloatingActionButton = v.findViewById(R.id.logout)
        logout.setOnClickListener({
            user = null
            SugarRecord.deleteAll(User::class.java)
            val intent = Intent(context, LoginActivity::class.java)
            context!!.startActivity(intent)
        })

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                UserFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
