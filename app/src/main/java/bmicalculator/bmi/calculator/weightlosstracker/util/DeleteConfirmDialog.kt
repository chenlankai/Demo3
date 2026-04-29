package bmicalculator.bmi.calculator.weightlosstracker.util


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import bmicalculator.bmi.calculator.weightlosstracker.databinding.DialogDeleteConfirmBinding


class DeleteConfirmDialog( context: Context) : BaseDialog(context){
    private val binding = DialogDeleteConfirmBinding.inflate(LayoutInflater.from(context))
    override fun getContentView(): View {

        return binding.root
    }

    override fun initView(savedInstanceState: Bundle?) {

        binding.deleteButton.setOnClickListener {


        }
        binding.cancelButton.setOnClickListener {

        }
    }


}