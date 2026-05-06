package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivityDataInputBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.adapter.AgeAdapter
import bmicalculator.bmi.calculator.weightlosstracker.util.dpToPx
import bmicalculator.bmi.calculator.weightlosstracker.util.setupMedicalInput
import bmicalculator.bmi.calculator.weightlosstracker.util.systemBarsTopPadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.SimpleDateFormat
import java.util.*

class DataInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataInputBinding
    private var isMale: Boolean = true
    private var selectedAge: Int = 25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.headerLayout.systemBarsTopPadding()


        setupAgeRecyclerView()
        setupUnitToggles()
        setupGenderSelection()
        setupDateTime()
        setupListeners()
        
        updateGenderUI()

        init()


    }
    private fun init(){
        binding.view1.setupMedicalInput("kg",1f,250f,2,true,6) //kg
        binding.view11.setupMedicalInput("cm",1f,250f,1,true,5) //cm
        binding.view2.setupMedicalInput("'",1f,8f,0,true,2) //ft
        binding.view3.setupMedicalInput("''",0f,11f,0,true,4) //in
    }
    private fun setupAgeRecyclerView() {
        val ages = (1..120).toList()
        val adapter = AgeAdapter(ages) { position ->
            binding.rvAge.smoothScrollToPosition(position)
        }
        binding.rvAge.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAge.adapter = adapter
        
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvAge)

        binding.rvAge.post {
            val itemWidth = 65.dpToPx(this)
            val padding = (binding.rvAge.width - itemWidth) / 2
            binding.rvAge.setPadding(padding, 0, padding, 0)
            binding.rvAge.clipToPadding = false
            binding.rvAge.scrollToPosition(24)
        }
        
        binding.rvAge.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val centerView = snapHelper.findSnapView(recyclerView.layoutManager)
                centerView?.let {
                    val position = recyclerView.layoutManager?.getPosition(it) ?: 0
                    if (position in ages.indices) {
                        selectedAge = ages[position]
                    }
                }
            }
        })
    }

    private fun setupUnitToggles() {

        binding.toggleUnit.post {
            updateToggleUI(binding.toggleUnit, binding.btnLb.id)
        }
        binding.toggleUnit1.post {
            updateToggleUI(binding.toggleUnit1, binding.btnFTin.id)
        }

        binding.toggleUnit.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {

                when (checkedId) {
                    binding.btnLb.id -> {
                        binding.view1.setupMedicalInput("lb", 2f, 551f, 2, true, 6)
                        binding.view1.setText("145.50lb")
                    }
                    binding.btnKg.id -> {
                        binding.view1.setupMedicalInput("kg", 1f, 250f, 2, true, 6)
                        binding.view1.setText("66.00kg")
                    }
                }

                updateToggleUI(group, checkedId)
            }
        }





        binding.toggleUnit1.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateToggleUI(group, checkedId)

                
                when (checkedId) {
                    binding.btnFTin.id -> {
                        binding.groupFtIn.visibility = View.VISIBLE
                        binding.groupCm.visibility = View.GONE

                    }

                    binding.btnCM.id -> {
                        binding.groupFtIn.visibility = View.GONE
                        binding.groupCm.visibility = View.VISIBLE
                        binding.view11.setText("170")
                    }
                }

                for (i in 0 until group.childCount) {
                    val child = group.getChildAt(i)
                    if (child is MaterialButton) {
                        updateButtonStyle(child, child.id == checkedId)
                    }
                }
            }
        }
    }

    private fun updateToggleUI(group: MaterialButtonToggleGroup, checkedId: Int) {
        for (i in 0 until group.childCount) {
            val button = group.getChildAt(i) as? MaterialButton ?: continue
            if (button.id == checkedId) {
                button.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                button.setTextColor(Color.BLACK)
                for (i in 0 until group.childCount) {
                    val child = group.getChildAt(i)
                    if (child is MaterialButton) {
                        updateButtonStyle(child, child.id == checkedId)
                    }
                }
            } else {
                button.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                button.setTextColor("#999999".toColorInt())
            }
        }
    }

    private fun setupGenderSelection() {
        binding.layoutMale.setOnClickListener {
            isMale = true
            updateGenderUI()
        }
        
        binding.layoutFemale.setOnClickListener {
            isMale = false
            updateGenderUI()
        }
    }

    private fun updateGenderUI() {
        if (isMale) {
            binding.ivCheckMale.visibility = View.VISIBLE
            binding.ivCheckFemale.visibility = View.GONE
            binding.layoutMale.alpha = 1.0f
            binding.layoutFemale.alpha = 0.6f
        } else {
            binding.ivCheckMale.visibility = View.GONE
            binding.ivCheckFemale.visibility = View.VISIBLE
            binding.layoutMale.alpha = 0.6f
            binding.layoutFemale.alpha = 1.0f
        }
    }

    private fun setupDateTime() {

    }

    private fun setupListeners() {
        binding.btnCalculate.setOnClickListener {
            val gender = if (isMale) "Male" else "Female"
            val age = selectedAge
            val weight = binding.view1.text.toString()
            val height = if (binding.toggleUnit1.checkedButtonId == binding.btnFTin.id) {
                "${binding.view2.text}${binding.view3.text}"
            } else {
                "${binding.view11.text} cm"
            }
            Log.d("DataInput", "Gender: $gender, Age: $age, Weight: $weight, Height: $height")
        }
    }
    private fun updateButtonStyle(button: MaterialButton, isSelected: Boolean) {
        val shapeModel = button.shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(100f)
            .build()
        button.shapeAppearanceModel = shapeModel

        if (isSelected) {
            button.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            button.setTextColor(Color.BLACK)
            button.alpha = 1.0f
        } else {
            button.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            button.setTextColor(Color.GRAY)
            button.alpha = 0.5f
        }
    }

}
