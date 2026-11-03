package dk.htmlwidget

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SetupActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences(
            "settings",
            MODE_PRIVATE
        )
    }

    private var localUri: Uri? =
        null

    private lateinit var widgetName: EditText
    private lateinit var url: EditText

    private lateinit var adBlock: Switch
    private lateinit var hideCookieBanners: Switch
    private lateinit var showUpdateStatus: Switch
    private lateinit var autoSwitchViews: Switch

    private lateinit var roundedCorners: Switch
    private lateinit var showBorder: Switch
    private lateinit var showBackground: Switch

    private lateinit var refreshSlider: SeekBar
    private lateinit var refreshLabel: TextView

    private val refreshValues =
        intArrayOf(
            15,
            30,
            45,
            60,
            120,
            180,
            240,
            300,
            360,
            420,
            480,
            540,
            600,
            660,
            720
        )

    private var appWidgetId =
        AppWidgetManager.INVALID_APPWIDGET_ID

    private val chooseHtmlRequest =
        42

    private val exportBackupRequest =
        43

    private val importBackupRequest =
        44

    private fun text(
        resId: Int,
        vararg formatArgs: Any
    ): String {

        val language =
            prefs.getString(
                "appLanguage",
                "da"
            ) ?: "da"

        val configuration =
            Configuration(
                resources.configuration
            )

        configuration.setLocale(
            Locale(language)
        )

        val localizedContext: Context =
            createConfigurationContext(
                configuration
            )

        return if (
            formatArgs.isEmpty()
        ) {

            localizedContext.getString(
                resId
            )

        } else {

            localizedContext.getString(
                resId,
                *formatArgs
            )
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        appWidgetId =
            intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val density =
            resources.displayMetrics.density

        fun dp(
            value: Int
        ): Int =
            (
                value *
                    density
                ).roundToInt()

        /*
         * ========================================
         * V3 FARVER
         * ========================================
         */
        val backgroundColor =
            android.graphics.Color.rgb(
                8,
                18,
                30
            )

        val cardColor =
            android.graphics.Color.rgb(
                18,
                38,
                56
            )

        val fieldColor =
            android.graphics.Color.rgb(
                24,
                48,
                69
            )

        val accentColor =
            android.graphics.Color.rgb(
                22,
                155,
                255
            )

        val primaryText =
            android.graphics.Color.WHITE

        val secondaryText =
            android.graphics.Color.rgb(
                190,
                205,
                220
            )

        val strokeColor =
            android.graphics.Color.rgb(
                79,
                111,
                139
            )

        /*
         * ========================================
         * V3 HJÆLPEFUNKTIONER
         * ========================================
         */
        fun roundedBackground(
            color: Int,
            radiusDp: Int = 18,
            strokeDp: Int = 0,
            stroke: Int = strokeColor
        ): android.graphics.drawable.GradientDrawable {

            return android.graphics.drawable
                .GradientDrawable()
                .apply {

                    setColor(
                        color
                    )

                    cornerRadius =
                        dp(
                            radiusDp
                        ).toFloat()

                    if (
                        strokeDp > 0
                    ) {

                        setStroke(
                            dp(
                                strokeDp
                            ),
                            stroke
                        )
                    }
                }
        }

        fun titleView(
            value: String
        ): TextView =

            TextView(
                this
            ).apply {

                text =
                    value

                setTextColor(
                    primaryText
                )

                textSize =
                    21f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }

        fun descriptionView(
            value: String
        ): TextView =

            TextView(
                this
            ).apply {

                text =
                    value

                setTextColor(
                    secondaryText
                )

                textSize =
                    13f

                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }

        fun card():
            LinearLayout =

            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(16),
                    dp(18),
                    dp(16)
                )

                background =
                    roundedBackground(
                        cardColor,
                        18,
                        1
                    )

                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {

                        setMargins(
                            0,
                            0,
                            0,
                            dp(14)
                        )
                    }
            }

        fun actionButton(
            label: String,
            primary: Boolean = false
        ): Button =

            Button(
                this
            ).apply {

                text =
                    label

                isAllCaps =
                    false

                textSize =
                    if (
                        primary
                    ) {
                        18f
                    } else {
                        14f
                    }

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.WHITE
                )

                background =
                    roundedBackground(
                        if (
                            primary
                        ) {
                            accentColor
                        } else {
                            fieldColor
                        },
                        if (
                            primary
                        ) {
                            16
                        } else {
                            12
                        },
                        if (
                            primary
                        ) {
                            0
                        } else {
                            1
                        }
                    )

                minHeight =
                    dp(
                        if (
                            primary
                        ) {
                            58
                        } else {
                            48
                        }
                    )

                setPadding(
                    dp(14),
                    0,
                    dp(14),
                    0
                )
            }

        fun styleSwitch(
            box: Switch
        ) {

            box.setTextColor(
                primaryText
            )

            box.textSize =
                15f

            box.setPadding(
                0,
                dp(5),
                0,
                dp(5)
            )

            if (
                android.os.Build.VERSION.SDK_INT >=
                21
            ) {

                box.thumbTintList =
                    android.content.res.ColorStateList(
                        arrayOf(
                            intArrayOf(
                                android.R.attr.state_checked
                            ),
                            intArrayOf()
                        ),
                        intArrayOf(
                            android.graphics.Color.WHITE,
                            secondaryText
                        )
                    )

                box.trackTintList =
                    android.content.res.ColorStateList(
                        arrayOf(
                            intArrayOf(
                                android.R.attr.state_checked
                            ),
                            intArrayOf()
                        ),
                        intArrayOf(
                            accentColor,
                            fieldColor
                        )
                    )
            }
        }

        fun sectionHeader(
            number: String,
            title: String,
            icon: String
        ): TextView =

            TextView(
                this
            ).apply {

                text =
                    "$icon  $number · $title"

                setTextColor(
                    primaryText
                )

                textSize =
                    20f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    0,
                    0,
                    0,
                    dp(12)
                )
            }

        /*
         * ========================================
         * HOVEDINDHOLD
         * ========================================
         */
        val content =
            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(18),
                    dp(16),
                    dp(28)
                )

                setBackgroundColor(
                    backgroundColor
                )
            }

        /*
         * ========================================
         * TOP
         * ========================================
         */
        val topRow =
            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    android.view.Gravity.CENTER_VERTICAL

                setPadding(
                    dp(4),
                    0,
                    dp(4),
                    dp(18)
                )
            }

        val topText =
            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.VERTICAL

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                addView(
                    TextView(
                        this@SetupActivity
                    ).apply {

                        text =
                            text(
                                R.string.app_name
                            )

                        setTextColor(
                            primaryText
                        )

                        textSize =
                            30f

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                addView(
                    TextView(
                        this@SetupActivity
                    ).apply {

                        text =
                            text(
                                R.string.setup_description
                            )

                        setTextColor(
                            secondaryText
                        )

                        textSize =
                            14f
                    }
                )
            }

        topRow.addView(
            topText
        )

        val savedLanguage =
            prefs.getString(
                "appLanguage",
                "da"
            ) ?: "da"

        /*
         * Sprogknappen skifter direkte
         * mellem dansk og engelsk.
         */
        val languageButton =
            actionButton(
                if (
                    savedLanguage == "en"
                ) {
                    "🇬🇧  EN"
                } else {
                    "🇩🇰  DK"
                }
            ).apply {

                layoutParams =
                    LinearLayout.LayoutParams(
                        dp(100),
                        dp(48)
                    )

                setOnClickListener {

                    val newLanguage =
                        if (
                            prefs.getString(
                                "appLanguage",
                                "da"
                            ) == "en"
                        ) {

                            "da"

                        } else {

                            "en"
                        }

                    prefs.edit()
                        .putString(
                            "appLanguage",
                            newLanguage
                        )
                        .apply()

                    recreate()
                }
            }

        topRow.addView(
            languageButton
        )

        content.addView(
            topRow
        )

        /*
         * ========================================
         * 1 · INDHOLD
         * ========================================
         */
        val sourceCard =
            card()

        sourceCard.addView(
            sectionHeader(
                "1",
                if (
                    savedLanguage == "en"
                ) {
                    "Content"
                } else {
                    "Indhold"
                },
                "🔗"
            )
        )

        sourceCard.addView(
            TextView(
                this
            ).apply {

                text =
                    if (
                        savedLanguage == "en"
                    ) {
                        "Widget name"
                    } else {
                        "Widgetnavn"
                    }

                setTextColor(
                    secondaryText
                )

                textSize =
                    13f
            }
        )

        widgetName =
            EditText(
                this
            ).apply {

                hint =
                    text(
                        R.string.widget_name_hint
                    )

                setText(
                    prefs.getString(
                        "widgetName_$appWidgetId",
                        ""
                    )
                )

                setSingleLine(
                    true
                )

                setTextColor(
                    primaryText
                )

                setHintTextColor(
                    secondaryText
                )

                textSize =
                    16f

                setPadding(
                    dp(14),
                    0,
                    dp(14),
                    0
                )

                background =
                    roundedBackground(
                        fieldColor,
                        12,
                        1
                    )

                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                    ).apply {

                        setMargins(
                            0,
                            dp(5),
                            0,
                            dp(12)
                        )
                    }
            }

        sourceCard.addView(
            widgetName
        )

        sourceCard.addView(
            TextView(
                this
            ).apply {

                text =
                    if (
                        savedLanguage == "en"
                    ) {
                        "Website address (URL)"
                    } else {
                        "Websiteadresse (URL)"
                    }

                setTextColor(
                    secondaryText
                )

                textSize =
                    13f
            }
        )

        url =
            EditText(
                this
            ).apply {

                hint =
                    text(
                        R.string.website_hint
                    )

                setText(
                    prefs.getString(
                        "url_$appWidgetId",
                        ""
                    )
                )

                setSingleLine(
                    true
                )

                setTextColor(
                    primaryText
                )

                setHintTextColor(
                    secondaryText
                )

                textSize =
                    16f

                setPadding(
                    dp(14),
                    0,
                    dp(14),
                    0
                )

                background =
                    roundedBackground(
                        fieldColor,
                        12,
                        1
                    )

                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                    ).apply {

                        setMargins(
                            0,
                            dp(5),
                            0,
                            dp(12)
                        )
                    }
            }

        sourceCard.addView(
            url
        )

        /*
         * VÆLG OMRÅDE
         */
        val selectAreaButton =
            actionButton(
                text(
                    R.string.select_area
                )
            )

        selectAreaButton
            .setOnClickListener {

                val website =
                    url.text
                        .toString()
                        .trim()

                if (
                    website.isEmpty()
                ) {

                    Toast.makeText(
                        this,
                        text(
                            R.string.enter_website_first
                        ),
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                prefs.edit()
                    .putString(
                        "url_$appWidgetId",
                        website
                    )
                    .apply()

                startActivity(
                    Intent(
                        this,
                        CropWebsiteActivity::class.java
                    ).apply {

                        putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            appWidgetId
                        )

                        putExtra(
                            "website",
                            website
                        )
                    }
                )
            }

        sourceCard.addView(
            selectAreaButton
        )

        /*
         * GENBRUG + HTML
         */
        val sourceButtons =
            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                setPadding(
                    0,
                    dp(8),
                    0,
                    0
                )
            }

        val reuseButton =
            actionButton(
                if (
                    savedLanguage == "en"
                ) {
                    "Reuse website"
                } else {
                    "Genbrug fra anden widget"
                }
            ).apply {

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        2f
                    ).apply {

                        marginEnd =
                            dp(4)
                    }

                setOnClickListener {

                    showReusableWebsiteDialog()
                }
            }

        val htmlButton =
            actionButton(
                if (
                    savedLanguage == "en"
                ) {
                    "HTML file"
                } else {
                    "HTML-fil"
                }
            ).apply {

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1f
                    ).apply {

                        marginStart =
                            dp(4)
                    }

                setOnClickListener {

                    val fileIntent =
                        Intent(
                            Intent.ACTION_OPEN_DOCUMENT
                        ).apply {

                            addCategory(
                                Intent.CATEGORY_OPENABLE
                            )

                            type =
                                "text/html"

                            addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            )
                        }

                    startActivityForResult(
                        fileIntent,
                        chooseHtmlRequest
                    )
                }
            }

        sourceButtons.addView(
            reuseButton
        )

        sourceButtons.addView(
            htmlButton
        )

        sourceCard.addView(
            sourceButtons
        )

        content.addView(
            sourceCard
        )

        /*
         * ========================================
         * 2 · OPDATERING
         * ========================================
         */
        val updateCard =
            card()

        updateCard.addView(
            sectionHeader(
                "2",
                if (
                    savedLanguage == "en"
                ) {
                    "Updates"
                } else {
                    "Opdatering"
                },
                "↻"
            )
        )

        val savedRefreshMinutes =
            prefs.getInt(
                "refreshMinutes_$appWidgetId",
                15
            )

        refreshLabel =
            TextView(
                this
            ).apply {

                setTextColor(
                    primaryText
                )

                textSize =
                    16f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    0,
                    0,
                    0,
                    dp(4)
                )
            }

        refreshSlider =
            SeekBar(
                this
            ).apply {

                max =
                    refreshValues.size -
                        1

                progress =
                    refreshValues
                        .indexOf(
                            savedRefreshMinutes
                        )
                        .coerceAtLeast(
                            0
                        )

                if (
                    android.os.Build.VERSION.SDK_INT >=
                    21
                ) {

                    progressTintList =
                        android.content.res
                            .ColorStateList
                            .valueOf(
                                accentColor
                            )

                    thumbTintList =
                        android.content.res
                            .ColorStateList
                            .valueOf(
                                accentColor
                            )
                }
            }

        refreshSlider
            .setOnSeekBarChangeListener(

                object :
                    SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {

                        updateRefreshLabel()
                    }

                    override fun onStartTrackingTouch(
                        seekBar: SeekBar?
                    ) {
                    }

                    override fun onStopTrackingTouch(
                        seekBar: SeekBar?
                    ) {
                    }
                }
            )

        updateRefreshLabel()

        updateCard.addView(
            refreshLabel
        )

        updateCard.addView(
            refreshSlider
        )

        showUpdateStatus =
            Switch(
                this
            ).apply {

                text =
                    text(
                        R.string.show_update_status
                    )

                isChecked =
                    prefs.getBoolean(
                        "showUpdateStatus_$appWidgetId",
                        true
                    )

                styleSwitch(
                    this
                )
            }

        updateCard.addView(
            showUpdateStatus
        )

        updateCard.addView(
            descriptionView(
                text(
                    R.string.update_status_description
                )
            )
        )

        content.addView(
            updateCard
        )

        /*
         * ========================================
         * 3 · SMART VISNING
         * ========================================
         */
        val smartCard =
            card()

        smartCard.addView(
            sectionHeader(
                "3",
                if (
                    savedLanguage == "en"
                ) {
                    "Smart display"
                } else {
                    "Smart visning"
                },
                "◈"
            )
        )

        adBlock =
            Switch(
                this
            ).apply {

                text =
                    text(
                        R.string.block_ads_tracking
                    )

                isChecked =
                    prefs.getBoolean(
                        "adblock",
                        true
                    )

                styleSwitch(
                    this
                )
            }

        smartCard.addView(
            adBlock
        )

        hideCookieBanners =
            Switch(
                this
            ).apply {

                text =
                    text(
                        R.string.hide_cookie_banners
                    )

                isChecked =
                    prefs.getBoolean(
                        "hideCookieBanners_$appWidgetId",
                        prefs.getBoolean(
                            "hideCookieBanners",
                            true
                        )
                    )

                styleSwitch(
                    this
                )
            }

        smartCard.addView(
            hideCookieBanners
        )

        smartCard.addView(
            descriptionView(
                text(
                    R.string.cookie_description
                )
            )
        )

        autoSwitchViews =
            Switch(
                this
            ).apply {

                text =
                    text(
                        R.string.auto_switch_views
                    )

                isChecked =
                    prefs.getBoolean(
                        "autoSwitchViews_$appWidgetId",
                        false
                    )

                styleSwitch(
                    this
                )
            }

        smartCard.addView(
            autoSwitchViews
        )

        smartCard.addView(
            descriptionView(
                text(
                    R.string.auto_switch_description
                )
            )
        )

        content.addView(
            smartCard
        )

        /*
         * ========================================
         * 4 · UDSEENDE
         * ========================================
         */
        val appearanceCard =
            card()

        appearanceCard.addView(
            sectionHeader(
                "4",
                text(
                    R.string.appearance_title
                ),
                "✦"
            )
        )

        appearanceCard.addView(
            descriptionView(
                text(
                    R.string.appearance_description
                )
            )
        )

        roundedCorners =
            Switch(
                this
            ).apply {

                text =
                    text(
                        R.string.rounded_corners
                    )

                isChecked =
                    prefs.getBoolean(
                        "roundedCorners_$appWidgetId",
                        true
                    )

                styleSwitch(
                    this
                )
            }

        showBorder =
            Switch(
                this
            ).apply {

                text =
                    text(
                        R.string.show_border
                    )

                isChecked =
                    prefs.getBoolean(
                        "showBorder_$appWidgetId",
                        false
                    )

                styleSwitch(
                    this
                )
            }

        showBackground =
            Switch(
                this
            ).apply {

                text =
                    text(
                        R.string.show_background
                    )

                isChecked =
                    prefs.getBoolean(
                        "showBackground_$appWidgetId",
                        true
                    )

                styleSwitch(
                    this
                )
            }

        appearanceCard.addView(
            roundedCorners
        )

        appearanceCard.addView(
            showBorder
        )

        appearanceCard.addView(
            showBackground
        )

        appearanceCard.addView(
            descriptionView(
                text(
                    R.string.appearance_widget_only
                )
            )
        )

        content.addView(
            appearanceCard
        )

        /*
         * ========================================
         * FORHÅNDSVISNING
         * ========================================
         */
        val previewCard =
            card()

        previewCard.addView(
            titleView(
                if (
                    savedLanguage == "en"
                ) {
                    "◉  Preview"
                } else {
                    "◉  Forhåndsvisning"
                }
            )
        )

        previewCard.addView(
            descriptionView(
                if (
                    savedLanguage == "en"
                ) {
                    "See the widget before saving your changes."
                } else {
                    "Se widgetten, før du gemmer dine ændringer."
                }
            )
        )

        previewCard.addView(
            actionButton(
                text(
                    R.string.show_preview
                )
            ).apply {

                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50)
                    ).apply {

                        topMargin =
                            dp(12)
                    }

                setOnClickListener {

                    startActivity(
                        Intent(
                            this@SetupActivity,
                            WidgetPreviewActivity::class.java
                        ).apply {

                            putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                appWidgetId
                            )
                        }
                    )
                }
            }
        )

        content.addView(
            previewCard
        )

        /*
         * ========================================
         * AVANCERET
         * ========================================
         */
        val advancedCard =
            card()

        val advancedHeader =
            TextView(
                this
            ).apply {

                text =
                    if (
                        savedLanguage == "en"
                    ) {
                        "⚙  Advanced                                      ▾"
                    } else {
                        "⚙  Avanceret                                    ▾"
                    }

                setTextColor(
                    primaryText
                )

                textSize =
                    19f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    0,
                    dp(2),
                    0,
                    dp(2)
                )
            }

        val advancedContent =
            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.VERTICAL

                visibility =
                    android.view.View.GONE

                setPadding(
                    0,
                    dp(12),
                    0,
                    0
                )
            }

        advancedContent.addView(
            descriptionView(
                text(
                    R.string.backup_description
                )
            )
        )

        advancedContent.addView(
            actionButton(
                text(
                    R.string.export_backup
                )
            ).apply {

                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48)
                    ).apply {

                        topMargin =
                            dp(10)
                    }

                setOnClickListener {

                    exportBackup()
                }
            }
        )

        advancedContent.addView(
            actionButton(
                text(
                    R.string.import_backup
                )
            ).apply {

                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48)
                    ).apply {

                        topMargin =
                            dp(8)
                    }

                setOnClickListener {

                    importBackup()
                }
            }
        )

        advancedHeader
            .setOnClickListener {

                advancedContent.visibility =
                    if (
                        advancedContent.visibility ==
                        android.view.View.VISIBLE
                    ) {

                        android.view.View.GONE

                    } else {

                        android.view.View.VISIBLE
                    }
            }

        advancedCard.addView(
            advancedHeader
        )

        advancedCard.addView(
            advancedContent
        )

        content.addView(
            advancedCard
        )

        /*
         * ========================================
         * GEM WIDGET
         * ========================================
         */
        content.addView(
            actionButton(
                "▣  ${
                    text(
                        R.string.save_update_widget
                    )
                }",
                primary = true
            ).apply {

                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(62)
                    ).apply {

                        topMargin =
                            dp(4)
                    }

                setOnClickListener {

                    saveWidgetSettings()
                }
            }
        )

        /*
         * ========================================
         * STATUSLINJE / SYSTEM BARS
         * ========================================
         *
         * V3 kører edge-to-edge på nyere Android.
         * Derfor bruger vi telefonens rigtige insets
         * i stedet for en fast topafstand.
         */
        val scrollView =
            ScrollView(
                this
            ).apply {

                setBackgroundColor(
                    backgroundColor
                )

                isFillViewport =
                    true

                clipToPadding =
                    false

                // Tablet landscape: keep cards readable instead of
                // stretching them across the entire screen.
                val availableWidthDp =
                    resources.configuration.screenWidthDp

                val contentWidth =
                    if (availableWidthDp >= 700) {
                        dp(620)
                    } else {
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    }

                addView(
                    content,
                    android.widget.FrameLayout.LayoutParams(
                        contentWidth,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.TOP or
                            android.view.Gravity.CENTER_HORIZONTAL
                    )
                )
            }

        androidx.core.view.ViewCompat
            .setOnApplyWindowInsetsListener(
                scrollView
            ) { view, insets ->

                val systemBars =
                    insets.getInsets(
                        androidx.core.view.WindowInsetsCompat
                            .Type
                            .systemBars()
                    )

                view.setPadding(
                    0,
                    systemBars.top,
                    0,
                    systemBars.bottom
                )

                insets
            }

        setContentView(
            scrollView
        )

        androidx.core.view.ViewCompat
            .requestApplyInsets(
                scrollView
            )
    }

    /*
     * ----------------------------------------
     * OPDATER REFRESH-TEKST
     * ----------------------------------------
     */
    private fun updateRefreshLabel() {

        val minutes =
            refreshValues[
                refreshSlider.progress
            ]

        refreshLabel.text =
            if (
                minutes < 60
            ) {

                text(
                    R.string.refresh_minutes,
                    minutes
                )

            } else {

                val hours =
                    minutes / 60

                if (
                    hours == 1
                ) {

                    text(
                        R.string.refresh_one_hour
                    )

                } else {

                    text(
                        R.string.refresh_hours,
                        hours
                    )
                }
            }
    }

    /*
     * ----------------------------------------
     * GEM WIDGET
     * ----------------------------------------
     */
    private fun saveWidgetSettings() {

        val widgetNameValue =
            widgetName.text
                .toString()
                .trim()

        val website =
            url.text
                .toString()
                .trim()

        prefs.edit().apply {

            putString(
                "widgetName_$appWidgetId",
                widgetNameValue
            )

            if (
                website.isNotEmpty()
            ) {

                putString(
                    "url_$appWidgetId",
                    website
                )

                remove(
                    "localUri_$appWidgetId"
                )

            } else if (
                localUri != null
            ) {

                putString(
                    "localUri_$appWidgetId",
                    localUri.toString()
                )

                remove(
                    "url_$appWidgetId"
                )
            }

            putBoolean(
                "adblock",
                adBlock.isChecked
            )

            putBoolean(
                "hideCookieBanners_$appWidgetId",
                hideCookieBanners.isChecked
            )

            putBoolean(
                "hideCookieBanners",
                hideCookieBanners.isChecked
            )

            putBoolean(
                "showUpdateStatus_$appWidgetId",
                showUpdateStatus.isChecked
            )

            putBoolean(
                "autoSwitchViews_$appWidgetId",
                autoSwitchViews.isChecked
            )

            putBoolean(
                "roundedCorners_$appWidgetId",
                roundedCorners.isChecked
            )

            putBoolean(
                "showBorder_$appWidgetId",
                showBorder.isChecked
            )

            putBoolean(
                "showBackground_$appWidgetId",
                showBackground.isChecked
            )

            putInt(
                "refreshMinutes_$appWidgetId",
                refreshValues[
                    refreshSlider.progress
                ]
            )

            apply()
        }

        scheduleRefresh()

        if (
            appWidgetId !=
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) {

            HtmlWidgetProvider.refresh(
                this,
                intArrayOf(
                    appWidgetId
                )
            )

            val resultValue =
                Intent().apply {

                    putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        appWidgetId
                    )
                }

            setResult(
                RESULT_OK,
                resultValue
            )

            Toast.makeText(
                this,
                text(
                    R.string.widget_saved
                ),
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }

    /*
     * ----------------------------------------
     * PLANLÆG AUTOMATISK REFRESH
     * ----------------------------------------
     */
    private fun scheduleRefresh() {

        if (
            appWidgetId ==
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) {

            return
        }

        val refreshMinutes =
            prefs.getInt(
                "refreshMinutes_$appWidgetId",
                15
            )

        val request =
            PeriodicWorkRequestBuilder<RefreshWorker>(
                refreshMinutes.toLong(),
                TimeUnit.MINUTES
            )
                .setInputData(
                    workDataOf(
                        AppWidgetManager.EXTRA_APPWIDGET_ID to
                            appWidgetId
                    )
                )
                .build()

        WorkManager
            .getInstance(
                this
            )
            .enqueueUniquePeriodicWork(
                "html-widget-refresh-$appWidgetId",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    /*
     * ----------------------------------------
     * EKSPORTER BACKUP
     * ----------------------------------------
     */
    private fun exportBackup() {

        if (
            appWidgetId ==
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) {

            Toast.makeText(
                this,
                text(
                    R.string.widget_not_found
                ),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent =
            Intent(
                Intent.ACTION_CREATE_DOCUMENT
            ).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type =
                    "application/json"

                putExtra(
                    Intent.EXTRA_TITLE,
                    "web-widget-backup-$appWidgetId.json"
                )
            }

        startActivityForResult(
            intent,
            exportBackupRequest
        )
    }

    /*
     * ----------------------------------------
     * IMPORTER BACKUP
     * ----------------------------------------
     */
    private fun importBackup() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {

                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type =
                    "application/json"
            }

        startActivityForResult(
            intent,
            importBackupRequest
        )
    }

    /*
     * ----------------------------------------
     * SKRIV BACKUP
     * ----------------------------------------
     */
    private fun writeBackup(
        uri: Uri
    ) {

        try {

            val json =
                JSONObject()

            json.put(
                "version",
                1
            )

            json.put(
                "widgetName",
                prefs.getString(
                    "widgetName_$appWidgetId",
                    ""
                ) ?: ""
            )

            json.put(
                "url",
                prefs.getString(
                    "url_$appWidgetId",
                    ""
                ) ?: ""
            )

            json.put(
                "localUri",
                prefs.getString(
                    "localUri_$appWidgetId",
                    ""
                ) ?: ""
            )

            json.put(
                "adblock",
                prefs.getBoolean(
                    "adblock",
                    true
                )
            )

            json.put(
                "hideCookieBanners",
                prefs.getBoolean(
                    "hideCookieBanners_$appWidgetId",
                    prefs.getBoolean(
                        "hideCookieBanners",
                        true
                    )
                )
            )

            json.put(
                "showUpdateStatus",
                prefs.getBoolean(
                    "showUpdateStatus_$appWidgetId",
                    true
                )
            )

            json.put(
                "autoSwitchViews",
                prefs.getBoolean(
                    "autoSwitchViews_$appWidgetId",
                    false
                )
            )

            json.put(
                "roundedCorners",
                prefs.getBoolean(
                    "roundedCorners_$appWidgetId",
                    true
                )
            )

            json.put(
                "showBorder",
                prefs.getBoolean(
                    "showBorder_$appWidgetId",
                    false
                )
            )

            json.put(
                "showBackground",
                prefs.getBoolean(
                    "showBackground_$appWidgetId",
                    true
                )
            )

            json.put(
                "refreshMinutes",
                prefs.getInt(
                    "refreshMinutes_$appWidgetId",
                    15
                )
            )

            /*
             * Crop-data
             */
            json.put(
                "cropEnabled",
                prefs.getBoolean(
                    "cropEnabled_$appWidgetId",
                    false
                )
            )

            json.put(
                "cropX",
                prefs.getFloat(
                    "cropX_$appWidgetId",
                    0f
                ).toDouble()
            )

            json.put(
                "cropY",
                prefs.getFloat(
                    "cropY_$appWidgetId",
                    0f
                ).toDouble()
            )

            json.put(
                "cropWidth",
                prefs.getFloat(
                    "cropWidth_$appWidgetId",
                    0f
                ).toDouble()
            )

            json.put(
                "cropHeight",
                prefs.getFloat(
                    "cropHeight_$appWidgetId",
                    0f
                ).toDouble()
            )

            json.put(
                "cropViewportWidth",
                prefs.getFloat(
                    "cropViewportWidth_$appWidgetId",
                    0f
                ).toDouble()
            )

            json.put(
                "cropViewportHeight",
                prefs.getFloat(
                    "cropViewportHeight_$appWidgetId",
                    0f
                ).toDouble()
            )

            /*
             * Flere crop-visninger.
             */
            val cropViewCount =
                prefs.getInt(
                    "cropViewCount_$appWidgetId",
                    1
                ).coerceAtLeast(1)

            json.put(
                "cropViewCount",
                cropViewCount
            )

            val cropViews =
                org.json.JSONArray()

            for (
                viewIndex in 1..cropViewCount
            ) {

                val viewJson =
                    JSONObject()

                val suffix =
                    if (
                        viewIndex == 1
                    ) {
                        ""
                    } else {
                        "_$viewIndex"
                    }

                viewJson.put(
                    "index",
                    viewIndex
                )

                viewJson.put(
                    "enabled",
                    prefs.getBoolean(
                        "cropEnabled${suffix}_$appWidgetId",
                        if (
                            viewIndex == 1
                        ) {
                            prefs.getBoolean(
                                "cropEnabled_$appWidgetId",
                                false
                            )
                        } else {
                            false
                        }
                    )
                )

                viewJson.put(
                    "x",
                    prefs.getFloat(
                        "cropX${suffix}_$appWidgetId",
                        if (
                            viewIndex == 1
                        ) {
                            prefs.getFloat(
                                "cropX_$appWidgetId",
                                0f
                            )
                        } else {
                            0f
                        }
                    ).toDouble()
                )

                viewJson.put(
                    "y",
                    prefs.getFloat(
                        "cropY${suffix}_$appWidgetId",
                        if (
                            viewIndex == 1
                        ) {
                            prefs.getFloat(
                                "cropY_$appWidgetId",
                                0f
                            )
                        } else {
                            0f
                        }
                    ).toDouble()
                )

                viewJson.put(
                    "width",
                    prefs.getFloat(
                        "cropWidth${suffix}_$appWidgetId",
                        if (
                            viewIndex == 1
                        ) {
                            prefs.getFloat(
                                "cropWidth_$appWidgetId",
                                0f
                            )
                        } else {
                            0f
                        }
                    ).toDouble()
                )

                viewJson.put(
                    "height",
                    prefs.getFloat(
                        "cropHeight${suffix}_$appWidgetId",
                        if (
                            viewIndex == 1
                        ) {
                            prefs.getFloat(
                                "cropHeight_$appWidgetId",
                                0f
                            )
                        } else {
                            0f
                        }
                    ).toDouble()
                )

                viewJson.put(
                    "viewportWidth",
                    prefs.getFloat(
                        "cropViewportWidth${suffix}_$appWidgetId",
                        if (
                            viewIndex == 1
                        ) {
                            prefs.getFloat(
                                "cropViewportWidth_$appWidgetId",
                                0f
                            )
                        } else {
                            0f
                        }
                    ).toDouble()
                )

                viewJson.put(
                    "viewportHeight",
                    prefs.getFloat(
                        "cropViewportHeight${suffix}_$appWidgetId",
                        if (
                            viewIndex == 1
                        ) {
                            prefs.getFloat(
                                "cropViewportHeight_$appWidgetId",
                                0f
                            )
                        } else {
                            0f
                        }
                    ).toDouble()
                )

                cropViews.put(
                    viewJson
                )
            }

            json.put(
                "cropViews",
                cropViews
            )

            contentResolver
                .openOutputStream(
                    uri
                )
                ?.bufferedWriter()
                ?.use {

                    it.write(
                        json.toString(
                            2
                        )
                    )
                }

            Toast.makeText(
                this,
                text(
                    R.string.backup_exported
                ),
                Toast.LENGTH_LONG
            ).show()

        } catch (_: Exception) {

            Toast.makeText(
                this,
                text(
                    R.string.backup_export_failed
                ),
                Toast.LENGTH_LONG
            ).show()
        }
    }    /*
     * ----------------------------------------
     * LÆS BACKUP
     * ----------------------------------------
     */
    private fun readBackup(
        uri: Uri
    ) {

        try {

            val jsonText =
                contentResolver
                    .openInputStream(
                        uri
                    )
                    ?.bufferedReader()
                    ?.use {

                        it.readText()
                    }
                    ?: throw IllegalStateException()

            val json =
                JSONObject(
                    jsonText
                )

            val editor =
                prefs.edit()

            val backupWidgetName =
                json.optString(
                    "widgetName",
                    ""
                )

            val backupUrl =
                json.optString(
                    "url",
                    ""
                )

            val backupLocalUri =
                json.optString(
                    "localUri",
                    ""
                )

            editor.putString(
                "widgetName_$appWidgetId",
                backupWidgetName
            )

            if (
                backupUrl.isNotBlank()
            ) {

                editor.putString(
                    "url_$appWidgetId",
                    backupUrl
                )

                editor.remove(
                    "localUri_$appWidgetId"
                )

            } else if (
                backupLocalUri.isNotBlank()
            ) {

                editor.putString(
                    "localUri_$appWidgetId",
                    backupLocalUri
                )

                editor.remove(
                    "url_$appWidgetId"
                )
            }

            editor.putBoolean(
                "adblock",
                json.optBoolean(
                    "adblock",
                    true
                )
            )

            editor.putBoolean(
                "hideCookieBanners_$appWidgetId",
                json.optBoolean(
                    "hideCookieBanners",
                    true
                )
            )

            editor.putBoolean(
                "hideCookieBanners",
                json.optBoolean(
                    "hideCookieBanners",
                    true
                )
            )

            editor.putBoolean(
                "showUpdateStatus_$appWidgetId",
                json.optBoolean(
                    "showUpdateStatus",
                    true
                )
            )

            editor.putBoolean(
                "autoSwitchViews_$appWidgetId",
                json.optBoolean(
                    "autoSwitchViews",
                    false
                )
            )

            editor.putBoolean(
                "roundedCorners_$appWidgetId",
                json.optBoolean(
                    "roundedCorners",
                    true
                )
            )

            editor.putBoolean(
                "showBorder_$appWidgetId",
                json.optBoolean(
                    "showBorder",
                    false
                )
            )

            editor.putBoolean(
                "showBackground_$appWidgetId",
                json.optBoolean(
                    "showBackground",
                    true
                )
            )

            editor.putInt(
                "refreshMinutes_$appWidgetId",
                json.optInt(
                    "refreshMinutes",
                    15
                )
            )

            /*
             * Legacy crop-data.
             */
            editor.putBoolean(
                "cropEnabled_$appWidgetId",
                json.optBoolean(
                    "cropEnabled",
                    false
                )
            )

            editor.putFloat(
                "cropX_$appWidgetId",
                json.optDouble(
                    "cropX",
                    0.0
                ).toFloat()
            )

            editor.putFloat(
                "cropY_$appWidgetId",
                json.optDouble(
                    "cropY",
                    0.0
                ).toFloat()
            )

            editor.putFloat(
                "cropWidth_$appWidgetId",
                json.optDouble(
                    "cropWidth",
                    0.0
                ).toFloat()
            )

            editor.putFloat(
                "cropHeight_$appWidgetId",
                json.optDouble(
                    "cropHeight",
                    0.0
                ).toFloat()
            )

            editor.putFloat(
                "cropViewportWidth_$appWidgetId",
                json.optDouble(
                    "cropViewportWidth",
                    0.0
                ).toFloat()
            )

            editor.putFloat(
                "cropViewportHeight_$appWidgetId",
                json.optDouble(
                    "cropViewportHeight",
                    0.0
                ).toFloat()
            )

            /*
             * Flere crop-visninger.
             */
            val cropViews =
                json.optJSONArray(
                    "cropViews"
                )

            if (
                cropViews != null &&
                cropViews.length() > 0
            ) {

                val cropViewCount =
                    json.optInt(
                        "cropViewCount",
                        cropViews.length()
                    ).coerceAtLeast(1)

                editor.putInt(
                    "cropViewCount_$appWidgetId",
                    cropViewCount
                )

                for (
                    arrayIndex in
                    0 until cropViews.length()
                ) {

                    val viewJson =
                        cropViews.optJSONObject(
                            arrayIndex
                        ) ?: continue

                    val viewIndex =
                        viewJson.optInt(
                            "index",
                            arrayIndex + 1
                        )

                    val suffix =
                        if (
                            viewIndex == 1
                        ) {
                            ""
                        } else {
                            "_$viewIndex"
                        }

                    editor.putBoolean(
                        "cropEnabled${suffix}_$appWidgetId",
                        viewJson.optBoolean(
                            "enabled",
                            false
                        )
                    )

                    editor.putFloat(
                        "cropX${suffix}_$appWidgetId",
                        viewJson.optDouble(
                            "x",
                            0.0
                        ).toFloat()
                    )

                    editor.putFloat(
                        "cropY${suffix}_$appWidgetId",
                        viewJson.optDouble(
                            "y",
                            0.0
                        ).toFloat()
                    )

                    editor.putFloat(
                        "cropWidth${suffix}_$appWidgetId",
                        viewJson.optDouble(
                            "width",
                            0.0
                        ).toFloat()
                    )

                    editor.putFloat(
                        "cropHeight${suffix}_$appWidgetId",
                        viewJson.optDouble(
                            "height",
                            0.0
                        ).toFloat()
                    )

                    editor.putFloat(
                        "cropViewportWidth${suffix}_$appWidgetId",
                        viewJson.optDouble(
                            "viewportWidth",
                            0.0
                        ).toFloat()
                    )

                    editor.putFloat(
                        "cropViewportHeight${suffix}_$appWidgetId",
                        viewJson.optDouble(
                            "viewportHeight",
                            0.0
                        ).toFloat()
                    )
                }

            } else {

                editor.putInt(
                    "cropViewCount_$appWidgetId",
                    1
                )
            }

            editor.apply()

            /*
             * Opdater felterne på skærmen.
             */
            widgetName.setText(
                backupWidgetName
            )

            url.setText(
                backupUrl
            )

            localUri =
                if (
                    backupLocalUri.isNotBlank()
                ) {

                    Uri.parse(
                        backupLocalUri
                    )

                } else {

                    null
                }

            adBlock.isChecked =
                json.optBoolean(
                    "adblock",
                    true
                )

            hideCookieBanners.isChecked =
                json.optBoolean(
                    "hideCookieBanners",
                    true
                )

            showUpdateStatus.isChecked =
                json.optBoolean(
                    "showUpdateStatus",
                    true
                )

            autoSwitchViews.isChecked =
                json.optBoolean(
                    "autoSwitchViews",
                    false
                )

            roundedCorners.isChecked =
                json.optBoolean(
                    "roundedCorners",
                    true
                )

            showBorder.isChecked =
                json.optBoolean(
                    "showBorder",
                    false
                )

            showBackground.isChecked =
                json.optBoolean(
                    "showBackground",
                    true
                )

            val importedRefreshMinutes =
                json.optInt(
                    "refreshMinutes",
                    15
                )

            refreshSlider.progress =
                refreshValues
                    .indexOf(
                        importedRefreshMinutes
                    )
                    .coerceAtLeast(
                        0
                    )

            updateRefreshLabel()

            Toast.makeText(
                this,
                text(
                    R.string.backup_imported
                ),
                Toast.LENGTH_LONG
            ).show()

        } catch (_: Exception) {

            Toast.makeText(
                this,
                text(
                    R.string.backup_import_failed
                ),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /*
     * ----------------------------------------
     * GENBRUG WEBSITE
     * ----------------------------------------
     */
    private fun showReusableWebsiteDialog() {

        val allPrefs =
            prefs.all

        val reusableIds =
            allPrefs.keys
                .filter {

                    it.startsWith(
                        "url_"
                    )

                }
                .mapNotNull {

                    it.removePrefix(
                        "url_"
                    )
                        .toIntOrNull()
                }
                .filter {

                    it !=
                        appWidgetId
                }
                .distinct()
                .sorted()

        if (
            reusableIds.isEmpty()
        ) {

            Toast.makeText(
                this,
                text(
                    R.string.no_reusable_websites
                ),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val labels =
            reusableIds
                .map { id ->

                    val name =
                        prefs.getString(
                            "widgetName_$id",
                            ""
                        )
                            ?.trim()
                            .orEmpty()

                    val website =
                        prefs.getString(
                            "url_$id",
                            ""
                        )
                            ?.trim()
                            .orEmpty()

                    if (
                        name.isNotBlank()
                    ) {

                        "$name\n$website"

                    } else {

                        website
                    }
                }
                .toTypedArray()

        AlertDialog.Builder(
            this
        )
            .setTitle(
                text(
                    R.string.reuse_website
                )
            )
            .setItems(
                labels
            ) { _, which ->

                val sourceId =
                    reusableIds[
                        which
                    ]

                reuseWebsiteSettings(
                    sourceId
                )
            }
            .setNegativeButton(
                android.R.string.cancel,
                null
            )
            .show()
    }

    /*
     * ----------------------------------------
     * KOPIER WEBSITE-INDSTILLINGER
     * ----------------------------------------
     */
    private fun reuseWebsiteSettings(
        sourceId: Int
    ) {

        val sourceUrl =
            prefs.getString(
                "url_$sourceId",
                ""
            )
                ?.trim()
                .orEmpty()

        if (
            sourceUrl.isBlank()
        ) {

            Toast.makeText(
                this,
                text(
                    R.string.no_reusable_websites
                ),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val editor =
            prefs.edit()

        editor.putString(
            "url_$appWidgetId",
            sourceUrl
        )

        editor.remove(
            "localUri_$appWidgetId"
        )

        editor.putBoolean(
            "hideCookieBanners_$appWidgetId",
            prefs.getBoolean(
                "hideCookieBanners_$sourceId",
                prefs.getBoolean(
                    "hideCookieBanners",
                    true
                )
            )
        )

        editor.putBoolean(
            "showUpdateStatus_$appWidgetId",
            prefs.getBoolean(
                "showUpdateStatus_$sourceId",
                true
            )
        )

        editor.putBoolean(
            "autoSwitchViews_$appWidgetId",
            prefs.getBoolean(
                "autoSwitchViews_$sourceId",
                false
            )
        )

        editor.putBoolean(
            "roundedCorners_$appWidgetId",
            prefs.getBoolean(
                "roundedCorners_$sourceId",
                true
            )
        )

        editor.putBoolean(
            "showBorder_$appWidgetId",
            prefs.getBoolean(
                "showBorder_$sourceId",
                false
            )
        )

        editor.putBoolean(
            "showBackground_$appWidgetId",
            prefs.getBoolean(
                "showBackground_$sourceId",
                true
            )
        )

        editor.putInt(
            "refreshMinutes_$appWidgetId",
            prefs.getInt(
                "refreshMinutes_$sourceId",
                15
            )
        )

        /*
         * Kopier crop-visninger.
         */
        val cropViewCount =
            prefs.getInt(
                "cropViewCount_$sourceId",
                1
            ).coerceAtLeast(1)

        editor.putInt(
            "cropViewCount_$appWidgetId",
            cropViewCount
        )

        for (
            viewIndex in 1..cropViewCount
        ) {

            val suffix =
                if (
                    viewIndex == 1
                ) {
                    ""
                } else {
                    "_$viewIndex"
                }

            editor.putBoolean(
                "cropEnabled${suffix}_$appWidgetId",
                prefs.getBoolean(
                    "cropEnabled${suffix}_$sourceId",
                    if (
                        viewIndex == 1
                    ) {
                        prefs.getBoolean(
                            "cropEnabled_$sourceId",
                            false
                        )
                    } else {
                        false
                    }
                )
            )

            editor.putFloat(
                "cropX${suffix}_$appWidgetId",
                prefs.getFloat(
                    "cropX${suffix}_$sourceId",
                    if (
                        viewIndex == 1
                    ) {
                        prefs.getFloat(
                            "cropX_$sourceId",
                            0f
                        )
                    } else {
                        0f
                    }
                )
            )

            editor.putFloat(
                "cropY${suffix}_$appWidgetId",
                prefs.getFloat(
                    "cropY${suffix}_$sourceId",
                    if (
                        viewIndex == 1
                    ) {
                        prefs.getFloat(
                            "cropY_$sourceId",
                            0f
                        )
                    } else {
                        0f
                    }
                )
            )

            editor.putFloat(
                "cropWidth${suffix}_$appWidgetId",
                prefs.getFloat(
                    "cropWidth${suffix}_$sourceId",
                    if (
                        viewIndex == 1
                    ) {
                        prefs.getFloat(
                            "cropWidth_$sourceId",
                            0f
                        )
                    } else {
                        0f
                    }
                )
            )

            editor.putFloat(
                "cropHeight${suffix}_$appWidgetId",
                prefs.getFloat(
                    "cropHeight${suffix}_$sourceId",
                    if (
                        viewIndex == 1
                    ) {
                        prefs.getFloat(
                            "cropHeight_$sourceId",
                            0f
                        )
                    } else {
                        0f
                    }
                )
            )

            editor.putFloat(
                "cropViewportWidth${suffix}_$appWidgetId",
                prefs.getFloat(
                    "cropViewportWidth${suffix}_$sourceId",
                    if (
                        viewIndex == 1
                    ) {
                        prefs.getFloat(
                            "cropViewportWidth_$sourceId",
                            0f
                        )
                    } else {
                        0f
                    }
                )
            )

            editor.putFloat(
                "cropViewportHeight${suffix}_$appWidgetId",
                prefs.getFloat(
                    "cropViewportHeight${suffix}_$sourceId",
                    if (
                        viewIndex == 1
                    ) {
                        prefs.getFloat(
                            "cropViewportHeight_$sourceId",
                            0f
                        )
                    } else {
                        0f
                    }
                )
            )
        }

        editor.apply()

        url.setText(
            sourceUrl
        )

        localUri =
            null

        hideCookieBanners.isChecked =
            prefs.getBoolean(
                "hideCookieBanners_$appWidgetId",
                true
            )

        showUpdateStatus.isChecked =
            prefs.getBoolean(
                "showUpdateStatus_$appWidgetId",
                true
            )

        autoSwitchViews.isChecked =
            prefs.getBoolean(
                "autoSwitchViews_$appWidgetId",
                false
            )

        roundedCorners.isChecked =
            prefs.getBoolean(
                "roundedCorners_$appWidgetId",
                true
            )

        showBorder.isChecked =
            prefs.getBoolean(
                "showBorder_$appWidgetId",
                false
            )

        showBackground.isChecked =
            prefs.getBoolean(
                "showBackground_$appWidgetId",
                true
            )

        val reusedRefreshMinutes =
            prefs.getInt(
                "refreshMinutes_$appWidgetId",
                15
            )

        refreshSlider.progress =
            refreshValues
                .indexOf(
                    reusedRefreshMinutes
                )
                .coerceAtLeast(
                    0
                )

        updateRefreshLabel()

        Toast.makeText(
            this,
            text(
                R.string.website_reused
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    /*
     * ----------------------------------------
     * RESULTATER FRA FILVÆLGER
     * ----------------------------------------
     */
    @Deprecated(
        "Deprecated in Java"
    )
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            resultCode !=
            RESULT_OK
        ) {

            return
        }

        val selectedUri =
            data?.data
                ?: return

        when (
            requestCode
        ) {

            chooseHtmlRequest -> {

                try {

                    contentResolver
                        .takePersistableUriPermission(
                            selectedUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                } catch (_: Exception) {
                }

                localUri =
                    selectedUri

                /*
                 * Lokal HTML og website-URL
                 * er alternative kilder.
                 */
                url.setText(
                    ""
                )

                Toast.makeText(
                    this,
                    text(
                        R.string.html_file_selected
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }

            exportBackupRequest -> {

                writeBackup(
                    selectedUri
                )
            }

            importBackupRequest -> {

                readBackup(
                    selectedUri
                )
            }
        }
    }
}
