package bmicalculator.bmi.calculator.weightlosstracker.util

import kotlin.math.roundToInt

object BmiConfigManager {
    // 颜色常量定义
    private const val COLOR_UNDERWEIGHT_DEEP = "#286DE6"
    private const val COLOR_UNDERWEIGHT_MED = "#349CEA"
    private const val COLOR_UNDERWEIGHT_LIGHT = "#5BB1F5"
    private const val COLOR_NORMAL = "#A8C526"
    private const val COLOR_OVERWEIGHT = "#FECD2E"
    private const val COLOR_OBESE_1 = "#FD9845"
    private const val COLOR_OBESE_2 = "#F67D3C"
    private const val COLOR_OBESE_3 = "#F04E46"

    data class BmiSection(
        val weightPercentage: Float,
        val color: String,
        val tickLabel: String,
        val minRange: Float?,
        val maxRange: Float?,
        val categoryName: String?
    )

    /**
     * 根据性别和年龄获取配置
     * @param gender 0 为男, 1 为女
     * @param age 年龄
     * @return Pair<List<BmiSection>, Pair<表盘Min, 表盘Max>>
     */
    fun getConfiguration(gender: Int, age: Int): Pair<List<BmiSection>, Pair<Float, Float>> {
        return if (age >= 21) {
            // 成年人对照表逻辑
            val minGrid = 15.6f
            val maxGrid = 40.3f
            val sections = listOf(
                createSection(minGrid, maxGrid, null, 16f, COLOR_UNDERWEIGHT_DEEP, "", "Very Severely Underweight"),
                createSection(minGrid, maxGrid, 16f, 17f, COLOR_UNDERWEIGHT_MED, "16", "Severely Underweight"),
                createSection(minGrid, maxGrid, 17f, 18.5f, COLOR_UNDERWEIGHT_LIGHT, "17", "Underweight"),
                createSection(minGrid, maxGrid, 18.5f, 25f, COLOR_NORMAL, "18.5", "Normal"),
                createSection(minGrid, maxGrid, 25f, 30f, COLOR_OVERWEIGHT, "25", "Overweight"),
                createSection(minGrid, maxGrid, 30f, 35f, COLOR_OBESE_1, "30", "Obese Class I"),
                createSection(minGrid, maxGrid, 35f, 40f, COLOR_OBESE_2, "35", "Obese Class II"),
                createSection(minGrid, maxGrid, 40f, null, COLOR_OBESE_3, "40", "Obese Class III")
            )
            sections to (minGrid to maxGrid)
        } else {
            // 非成年人逻辑：根据性别和年龄提取阈值
            getChildConfig(gender, age)
        }
    }

    private fun getChildConfig(gender: Int, age: Int): Pair<List<BmiSection>, Pair<Float, Float>> {
        // 以下数据基于“数值对照表.csv”提取的核心节点
        // thresholds 格式: Underweight边界, Normal边界, Overweight边界
        val (minGrid, maxGrid, thresholds) = when (gender) {
            0 -> when (age) { // 男性

                2 -> Triple(14f, 20f, listOf(14.8f, 18.2f, 19.3f))
                3 -> Triple(13f, 19f, listOf(14.4f, 17.4f, 18.3f))
                4 -> Triple(13f, 19f, listOf(14.0f, 16.9f, 18.0f))
                5 -> Triple(13f, 19f, listOf(13.8f, 16.8f, 18.1f))
                6 -> Triple(13f, 20f, listOf(13.7f, 17.0f, 18.6f))
                7 -> Triple(13f, 20f, listOf(13.6f, 17.4f, 19.2f))
                8 -> Triple(13f, 21f, listOf(13.7f, 17.8f, 20.0f))
                9 -> Triple(13f, 22f, listOf(14.0f, 18.6f, 21.1f))
                10 -> Triple(13f, 23f, listOf(14.2f, 19.3f, 22.2f))
                11 -> Triple(13f, 24f, listOf(14.5f, 20.0f, 23.2f))
                12 -> Triple(14f, 25f, listOf(15.0f, 21.0f, 24.2f))
                13 -> Triple(14f, 26f, listOf(15.5f, 21.7f, 25.4f))
                14 -> Triple(15f, 27f, listOf(16.0f, 22.6f, 26.0f))
                15 -> Triple(15f, 28f, listOf(16.5f, 23.5f, 26.8f))
                16 -> Triple(16f, 29f, listOf(17.1f, 24.2f, 27.7f))
                17 -> Triple(17f, 29f, listOf(17.6f, 24.8f, 28.3f))
                18 -> Triple(17f, 30f, listOf(18.3f, 25.6f, 29.0f))
                19 -> Triple(17f, 31f, listOf(18.5f, 26.4f, 29.8f))
                20 -> Triple(17f, 32f, listOf(18.5f, 27.2f, 30.7f))
                else -> Triple(17f, 32f, listOf(18.5f, 27.2f, 30.7f))
            }
            else -> when (age) { // 女性
                2 -> Triple(13f, 20f, listOf(14.4f, 17.9f, 19.0f))
                3 -> Triple(13f, 19f, listOf(14.0f, 17.1f, 18.2f))
                4 -> Triple(13f, 19f, listOf(13.7f, 16.7f, 17.9f))
                5 -> Triple(13f, 19f, listOf(13.5f, 16.7f, 18.2f))
                6 -> Triple(13f, 20f, listOf(13.4f, 17.0f, 18.7f))
                7 -> Triple(13f, 21f, listOf(13.4f, 17.5f, 19.6f))
                8 -> Triple(13f, 22f, listOf(13.6f, 18.3f, 20.5f))
                9 -> Triple(13f, 23f, listOf(13.8f, 19.1f, 21.7f))
                10 -> Triple(13f, 24f, listOf(14.0f, 19.9f, 22.9f))
                11 -> Triple(14f, 26f, listOf(14.8f, 21.6f, 25.1f))
                12 -> Triple(14f, 27f, listOf(14.8f, 21.7f, 25.1f))
                13 -> Triple(15f, 28f, listOf(15.3f, 22.5f, 26.2f))
                14 -> Triple(15f, 29f, listOf(15.8f, 23.2f, 27.2f))
                15 -> Triple(16f, 30f, listOf(16.3f, 24.0f, 28.0f))
                16 -> Triple(16f, 31f, listOf(16.8f, 24.6f, 28.8f))
                17 -> Triple(17f, 32f, listOf(17.2f, 25.2f, 29.6f))
                18 -> Triple(17f, 33f, listOf(17.5f, 25.7f, 30.3f))
                19 -> Triple(17f, 34f, listOf(17.8f, 26.1f, 30.9f))
                20 -> Triple(17f, 35f, listOf(17.8f, 26.5f, 31.7f))
                else -> Triple(17f, 35f, listOf(17.8f, 26.5f, 31.7f))
            }
        }

        val sections = listOf(
            createSection(minGrid, maxGrid, null, thresholds[0], COLOR_UNDERWEIGHT_MED, "", "Underweight"),
            createSection(minGrid, maxGrid, thresholds[0], thresholds[1], COLOR_NORMAL, thresholds[0].toString(), "Normal"),
            createSection(minGrid, maxGrid, thresholds[1], thresholds[2], COLOR_OVERWEIGHT, thresholds[1].toString(), "Overweight"),
            createSection(minGrid, maxGrid, thresholds[2], null, COLOR_OBESE_1, thresholds[2].toString(), "Obese Class I")
        )
        return sections to (minGrid to maxGrid)
    }

    /**
     * 工具方法：自动根据数值区间计算 UI 百分比权重，确保角度线性对应
     */
    private fun createSection(
        gridMin: Float,
        gridMax: Float,
        min: Float?,
        max: Float?,
        color: String,
        label: String,
        category: String
    ): BmiSection {
        val totalRange = gridMax - gridMin
        // 确定该段在表盘上的实际起始和结束数值
        val startValue = min ?: gridMin
        val endValue = max ?: gridMax

        // 计算百分比权重：(区间长度 / 总长度) * 100
        val weight = ((endValue - startValue) / totalRange) * 100f

        return BmiSection(weight, color, label, min, max, category)
    }
}