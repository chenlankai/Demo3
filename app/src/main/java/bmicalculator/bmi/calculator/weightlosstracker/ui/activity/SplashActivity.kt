package bmicalculator.bmi.calculator.weightlosstracker.ui.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.PathInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bmicalculator.bmi.calculator.weightlosstracker.data.database.AppDatabase
import bmicalculator.bmi.calculator.weightlosstracker.databinding.ActivitySplashBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import bmicalculator.bmi.calculator.weightlosstracker.ui.base.BaseActivity

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        val bezierInterpolator = PathInterpolator(0.25f, 0f, 0.1f, 0.1f)

        val dial = binding.iconSplashLogo
        val pointer = binding.iconBmiPointer
        val title = binding.iconSplashTraining

        // Initial state
        dial.alpha = 0f
        dial.translationY = 100f
        title.alpha = 0f
        title.translationY = 100f
        pointer.rotation = -40f

        pointer.post {
            pointer.pivotX = pointer.width / 2f
            pointer.pivotY = pointer.height.toFloat()
        }

        val dialFade = ObjectAnimator.ofFloat(dial, "alpha", 0f, 1f)
        val dialMove = ObjectAnimator.ofFloat(dial, "translationY", 100f, 0f)
        val titleFade = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f)
        val titleMove = ObjectAnimator.ofFloat(title, "translationY", 100f, 0f)

        val fastRotate = ObjectAnimator.ofFloat(pointer, "rotation", -40f, 40f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 1000
        }

        val bounceRotate = ObjectAnimator.ofFloat(pointer, "rotation", 40f, -40f).apply {
            interpolator = bezierInterpolator
            startDelay = 1000
            duration = 1000
        }

        val phase1 = AnimatorSet().apply {
            playTogether(dialFade, dialMove, titleFade, titleMove, fastRotate)
        }

        val allAnimations = AnimatorSet().apply {
            playSequentially(phase1, bounceRotate)
        }

        allAnimations.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                lifecycleScope.launch {
                    val records = AppDatabase.getDatabase(this@SplashActivity).bmiDao().getAllRecords().first()
                    if (records.isEmpty()) {
                        startActivity(Intent(this@SplashActivity, DataInputActivity::class.java))
                    } else {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    }
                    finish()
                }
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        allAnimations.start()
    }
}
