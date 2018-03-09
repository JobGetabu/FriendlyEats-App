package com.job.friendlyeats

import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import me.zhanghai.android.materialratingbar.MaterialRatingBar

/**
 * Created by JOB on 3/5/2018.
 */
class RestrauntFragment : DialogFragment() {

    val TAG = "RestaurantDialogueFragment"

    interface RestaurantListner {
        fun onRestaurantAdd()
    }

    lateinit var mRestaurantListner: RestaurantListner

    @BindView(R.id.floatingActionButton)
    internal var floatingActionButton: FloatingActionButton? = null
    @BindView(R.id.textview_restrant_name)
    internal var restaurantName: EditText? = null
    @BindView(R.id.textView_city)
    internal var location: EditText? = null
    @BindView(R.id.textView_food_category)
    internal var foodCategory: EditText? = null
    @BindView(R.id.ratingBar)
    internal var ratingBar: MaterialRatingBar? = null
    @BindView(R.id.button_cancel)
    internal var btnCancel: Button? = null
    @BindView(R.id.button_save)
    internal var btnSave: Button? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle): View? {
        //return super.onCreateView(inflater, container, savedInstanceState);
        val v = inflater.inflate(R.layout.restraunt_input, container, false)
        ButterKnife.bind(this, v)

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is RestaurantListner) {
            mRestaurantListner = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    @OnClick(R.id.button_save)
    fun saveClick() {
        Snackbar.make(view!!, "TODO Save Restaurant", Snackbar.LENGTH_LONG).show()
        dismiss()
    }

    @OnClick(R.id.button_cancel)
    fun cancelClick() {
        //new MainActivity().showTodoToast();
        dismiss()
    }
}