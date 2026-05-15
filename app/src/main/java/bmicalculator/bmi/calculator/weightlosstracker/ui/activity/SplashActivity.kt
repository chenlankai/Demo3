package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.PathInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import bmicalculator.bmi.calculator.weightlosstracker.R
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivitySplashBinding
import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.navigationBarColor = Color.TRANSPARENT // 设置导航栏颜色为透明
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false // 禁用 Q 以后系统强制的对比度保护层
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            insets
        }

        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        val bezierInterpolator = PathInterpolator(0.25f, 0f, 0.1f, 0.1f)

        val dial = binding.iconSplashLogo
        val pointer = binding.iconBmiPointer
        val title = binding.iconSplashTraining


        dial.post {
            // 1. 设置初始状态
            dial.alpha = 0f
            dial.translationY = 400f
            title.alpha = 0f
            title.translationY = 400f

            pointer.translationY = 400f
            pointer.rotation = -40f

            pointer.pivotX = pointer.width / 2f
            pointer.pivotY = pointer.height.toFloat()



            val dialFade = ObjectAnimator.ofFloat(dial, "alpha", 0f, 1f).apply { duration = 1000 }
            val titleFade = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).apply { duration = 1000 }
            val pointerFade = ObjectAnimator.ofFloat(pointer, "alpha", 0f, 1f).apply { duration = 1000 }



            val dialMove = ObjectAnimator.ofFloat(dial, "translationY", 400f, 0f).apply { duration = 1000 }
            val titleMove = ObjectAnimator.ofFloat(title, "translationY", 400f, 0f).apply { duration = 1000 }
            val pointerMove = ObjectAnimator.ofFloat(pointer, "translationY", 400f, 0f).apply { duration = 1000 }


            val fastRotate = ObjectAnimator.ofFloat(pointer, "rotation", -40f, 40f).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 1000
            }


            val bounceRotate = ObjectAnimator.ofFloat(pointer, "rotation", 40f, -40f).apply {
                interpolator = bezierInterpolator
                duration = 1000
            }


            val phase1 = AnimatorSet().apply {
                playTogether(dialFade, dialMove, titleFade, titleMove, pointerMove,pointerFade, fastRotate)
            }

            val allAnimations = AnimatorSet().apply {
                playSequentially(phase1, bounceRotate)
            }

            // 4. 设置监听并启动
            allAnimations.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    navigateToNextScreen()
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })

            allAnimations.start()
        }
    }

    private fun navigateToNextScreen() {
        lifecycleScope.launch {
            val records = AppDatabase.getDatabase(this@SplashActivity).bmiDao().getAllRecords().first()
            val targetActivity = if (records.isEmpty()) {
                DataInputActivity::class.java
            } else {
                MainActivity::class.java
            }
            startActivity(Intent(this@SplashActivity, targetActivity))
            finish()
        }
    }
}