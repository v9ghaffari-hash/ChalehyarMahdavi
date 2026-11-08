قدم یار — نسخه ۴۳

این پروژه نسخه مستقل اندروید برنامه قدم یار است و با Kotlin و Jetpack Compose ساخته شده است.

شناسه برنامه: ir.chillehyar.qadamyar.v43
نسخه: 43.0-qadam-yar (versionCode=43)

رفع خطای گزارش‌شده در GitHub Actions
در گزارش build نسخه ۴۲، خطای قطعی زیر ثبت شده بود:
MainActivity.kt:748:58 Unresolved reference 'TopLeft'
علت: Alignment.TopLeft در Jetpack Compose تعریف نشده است.
اصلاح: استفاده از Alignment.TopStart. این اصلاح در سورس نسخه ۴۳ اعمال شده است.

ساخت APK با GitHub Actions
۱) فایل ZIP را در یک پوشه خالی استخراج کن.
۲) وارد پوشه QadamYar_V43 شو.
۳) محتویات داخل این پوشه (از جمله پوشه‌های app و .github و فایل‌های Gradle) را در ریشه مخزن GitHub قرار بده؛ خود پوشه QadamYar_V43 را به‌عنوان یک زیرپوشه در مخزن قرار نده.
۴) به شاخه main پوش کن، یا از تب Actions گردش کار Build QadamYar V43 APK را دستی اجرا کن.
۵) پس از موفقیت گردش کار، artifact با نام QadamYar-V43-debug-apk را دریافت کن؛ فایل APK داخل آن است.

ساخت محلی در Android Studio
پروژه را از همین پوشه باز کن، منتظر Gradle Sync بمان و از مسیر Build > Build Bundle(s) / APK(s) > Build APK(s) خروجی debug بگیر.

نصب مستقل
شناسه بسته v43 با v42 متفاوت است؛ بنابراین در حالت عادی کنار آن نصب می‌شود و جایگزینش نمی‌کند. به دلیل جداسازی داده در اندروید، اطلاعات برنامه قبلی خودکار منتقل نمی‌شوند؛ از فایل پشتیبان نسخه قبلی استفاده کن و آن را در v43 بازیابی کن.

نکته آزمون
گزارش دریافتی نشان می‌داد مراحل راه‌اندازی Java، Android SDK و Gradle موفق بوده و کامپایل Kotlin فقط به‌علت Alignment.TopLeft شکست خورده است. این سورس اصلاح شده، اما در محیط فعلی ابزار Android SDK/Gradle برای اجرای build نهایی موجود نیست؛ بنابراین موفقیت APK را تا اجرای workflow نسخه ۴۳ نمی‌توان تأیید کرد.
