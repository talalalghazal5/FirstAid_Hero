package com.example.firstaidhero.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.firstaidhero.models.TreatedCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "first_aid_hero.db";
    private static final int DATABASE_VERSION = 4; // تم رفع الإصدار لتحديث البيانات
    private final Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        copyDatabaseIfMissing();
    }

    private void copyDatabaseIfMissing() {
        File dbFile = mContext.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs();
            try {
                InputStream input = mContext.getAssets().open(DATABASE_NAME);
                OutputStream output = new FileOutputStream(dbFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                output.flush();
                output.close();
                input.close();
            } catch (IOException e) {
                Log.e("DatabaseHelper", "ملف assets غير موجود، سيتم بناء القاعدة برمجياً");
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS emergencies (case_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, danger_level TEXT, symptoms TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS steps (step_id INTEGER PRIMARY KEY AUTOINCREMENT, case_id INTEGER, step_number INTEGER, instruction_text TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS custom_contacts (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS treated_cases (id INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT, date TEXT, procedures TEXT, photo_path TEXT)");
        
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // حذف الجداول القديمة لإعادة بنائها بالبيانات الجديدة
        db.execSQL("DROP TABLE IF EXISTS emergencies");
        db.execSQL("DROP TABLE IF EXISTS steps");
        // لا نحذف treated_cases أو custom_contacts لكي لا يفقد المستخدم بياناته الخاصة
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE treated_cases ADD COLUMN photo_path TEXT");
        }
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // 1-5 الحالات الأساسية
        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (1, 'الاختناق (انسداد مجرى الهواء)', 'حرِج', 'عدم القدرة على التنفس، مسك الرقبة باليدين، تغير لون الوجه للأزرق')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (1, 1, 'قف خلف المصاب ولف ذراعيك حول خصره.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (1, 2, 'اصنع قبضة بيد واحدة وضعها فوق سرة الشخص مباشرة.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (1, 3, 'اضغط بقوة للداخل وللأعلى (مناورة هيمليك) حتى يخرج الجسم الغريب.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (2, 'النزيف الخارجي الحاد', 'حرِج', 'تدفق مستمر للدم، جرح عميق، نبض سريع، شحوب')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (2, 1, 'اضغط مباشرة على الجرح بقطعة قماش نظيفة أو ضمادة معقمة.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (2, 2, 'ارفع الطرف المصاب فوق مستوى القلب لتقليل قوة النزيف.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (3, 'الحروق الحرارية', 'متوسط', 'ألم شديد، احمرار الجلد، تورم، ظهور فقاعات مائية')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (3, 1, 'برد الحرق بماء فاتر جارٍ لمدة 15 دقيقة على الأقل.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (3, 2, 'غطِّ المنطقة بشاش معقم ولا تستخدم معجون الأسنان أو الزبدة.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (4, 'الكسور وإصابات العظام', 'متوسط', 'ألم شديد عند الحركة، تشوه في شكل الطرف، تورم')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (4, 1, 'ثبت العضو المصاب كما هو، ولا تحاول إعادة العظم المكسور لمكانه.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (4, 2, 'استخدم جبيرة مؤقتة لتثبيت المفاصل المحيطة بالكسر.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (5, 'ضربة الشمس', 'حرِج', 'حرارة جسم عالية، جفاف الجلد، صداع، تشوش ذهني')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (5, 1, 'انقل المصاب فوراً إلى مكان بارد أو مظلل.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (5, 2, 'برد جسم المصاب باستخدام الماء البارد أو كمادات ثلج.')");

        // 6-10
        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (6, 'الرعاف (نزيف الأنف)', 'طفيف', 'خروج دم من إحدى فتحتي الأنف أو كلتيهما')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (6, 1, 'اجعل المصاب يجلس ويحني رأسه قليلاً إلى الأمام.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (6, 2, 'اضغط بإحكام على الجزء اللين من الأنف لمدة 10 دقائق.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (7, 'فقدان الوعي (الإغماء)', 'متوسط', 'سقوط مفاجئ، شحوب، برودة الجلد، نبض ضعيف')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (7, 1, 'ضع المصاب مستلقياً على ظهره وارفع قدميه حوالي 30 سم.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (8, 'لدغات الأفاعي والعقارب', 'حرِج', 'أثر أنياب، ألم حاد، تورم سريع، غثيان')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (8, 1, 'حافظ على هدوء المصاب وثبت الطرف المصاب بمستوى أدنى من القلب.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (9, 'التسمم (ابتلاع مواد كيميائية)', 'حرِج', 'غثيان، تقيؤ، حروق حول الفم، رائحة غريبة')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (9, 1, 'اتصل بمركز السموم فوراً ولا تجبر المصاب على التقيؤ.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (10, 'النوبة القلبية', 'حرِج', 'ألم ضاغط في الصدر، ضيق تنفس، تعرق بارد')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (10, 1, 'اجعل المصاب يجلس في وضعية مريحة واتصل بالإسعاف فوراً.')");

        // 11-15
        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (11, 'الصعق الكهربائي', 'حرِج', 'حروق، فقدان وعي، توقف تنفس، تشنجات')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (11, 1, 'افصل مصدر الكهرباء أو ابعد المصاب بأداة خشبية جافة.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (11, 2, 'تحقق من النبض والتنفس وابدأ الإنعاش القلبي إذا لزم الأمر.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (12, 'الغرق', 'حرِج', 'توقف تنفس، شحوب، سعال رغوي')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (12, 1, 'أخرج المصاب من الماء وابدأ بـ 5 أنفاس إنقاذ فوراً.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (13, 'التسمم الغذائي', 'متوسط', 'مغص شديد، تقيؤ، إسهال، ارتفاع حرارة')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (13, 1, 'امنح المصاب رشفات صغيرة من الماء أو محلول الجفاف لمنع الجفاف.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (14, 'السكتة الدماغية (جلطة الدماغ)', 'حرِج', 'تدلي الوجه، ضعف في الذراع، صعوبة في الكلام')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (14, 1, 'اطلب الإسعاف فوراً، وسجل وقت ظهور الأعراض.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (15, 'نوبة الصرع (التشنجات)', 'متوسط', 'فقدان وعي مفاجئ، تشنج العضلات، رغوة بالفم')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (15, 1, 'ابعد الأجسام الصلبة عن المصاب وضع شيئاً ليناً تحت رأسه.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (15, 2, 'لا تضع أي شيء في فمه ولا تحاول تقييد حركته.')");

        // 16-20
        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (16, 'هبوط السكر المفاجئ', 'متوسط', 'تعرق، رعشة، جوع شديد، دوخة')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (16, 1, 'أعطِ المصاب ملعقة عسل أو نصف كوب عصير محلى.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (17, 'نوبة الربو الحادة', 'متوسط', 'ضيق تنفس، سعال، صوت صفير عند التنفس')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (17, 1, 'ساعد المصاب على الجلوس واستخدام بخاخ الربو.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (18, 'التواء المفاصل', 'طفيف', 'ألم عند الحركة، تورم، كدمات')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (18, 1, 'أرح المفصل وضع كمادات ثلج لمدة 15 دقيقة.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (19, 'الجروح والخدوش البسيطة', 'طفيف', 'نزف طفيف، ألم سطحي')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (19, 1, 'اغسل الجرح بالماء والصابون وضع مرهم مطهر.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (20, 'الحساسية المفرطة', 'حرِج', 'تورم اللسان، ضيق تنفس، طفح جلدي مفاجئ')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (20, 1, 'اتصل بالإسعاف واستخدم حقنة الأدرينالين إن توفرت.')");

        // 21-25
        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (21, 'عضات الحيوانات', 'متوسط', 'أثر أسنان، جرح، نزيف')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (21, 1, 'اغسل الجرح بالماء الجاري والصابون جيداً وتوجه للطبيب.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (22, 'جسم غريب في العين', 'طفيف', 'إحمرار، دموع، وخز')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (22, 1, 'لا تفرك العين، اغسلها بماء فاتر ونظيف.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (23, 'آلام البطن الحادة', 'متوسط', 'تشنجات قوية، غثيان')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (23, 1, 'اجعل المصاب يرتاح، ولا تعطه أي مسكنات دون استشارة.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (24, 'حروق المواد الكيميائية', 'حرِج', 'تآكل الجلد، إحمرار حارق')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (24, 1, 'اغسل المنطقة بالماء الجاري لمدة 20 دقيقة.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (25, 'انخفاض حرارة الجسم', 'متوسط', 'رعشة، خمول، نبض ضعيف')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (25, 1, 'لف المصاب ببطانية دافئة وانقله لمكان جاف.')");

        // 26-30 (البيانات الجديدة)
        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (26, 'إصابات الرأس والارتجاج', 'حرِج', 'دوخة، غثيان، فقدان ذاكرة مؤقت، صداع شديد')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (26, 1, 'اجعل المصاب يستلقي بهدوء وراقب درجة وعيه.')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (26, 2, 'إذا تقيأ المصاب أو فقد الوعي، اطلب الإسعاف فوراً.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (27, 'ارتفاع السكر الحاد', 'متوسط', 'عطش شديد، كثرة تبول، رائحة فم تشبه الفاكهة')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (27, 1, 'شجع المصاب على شرب الماء ونقله للمستشفى.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (28, 'نوبة الهلع والقلق', 'طفيف', 'تسارع ضربات القلب، تعرق، خوف شديد، ضيق تنفس')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (28, 1, 'طمئن المصاب واجعله يتنفس ببطء وعمق.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (29, 'الحمى الشديدة', 'متوسط', 'ارتفاع حرارة الجسم، قشعريرة، تعب')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (29, 1, 'استخدم كمادات ماء فاتر وتجنب الماء البارد جداً.')");

        db.execSQL("INSERT INTO emergencies (case_id, title, danger_level, symptoms) VALUES (30, 'إصابات الظهر والعمود الفقري', 'حرِج', 'ألم شديد في الظهر، فقدان الإحساس في الأطراف')");
        db.execSQL("INSERT INTO steps (case_id, step_number, instruction_text) VALUES (30, 1, 'لا تحرك المصاب نهائياً وانتظر وصول الإسعاف.')");
    }

    public Cursor searchEmergencies(String query) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT * FROM emergencies WHERE title LIKE ?", new String[]{"%" + query + "%"});
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getStepsForCase(int caseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM steps WHERE case_id = ? ORDER BY step_number ASC", new String[]{String.valueOf(caseId)});
    }

    public void addTreatedCase(String desc, String date, String proc, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", desc);
        values.put("date", date);
        values.put("procedures", proc);
        values.put("photo_path", photoPath);
        db.insert("treated_cases", null, values);
    }

    public void updateTreatedCase(int id, String desc, String date, String proc, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("description", desc);
        values.put("date", date);
        values.put("procedures", proc);
        values.put("photo_path", photoPath);
        db.update("treated_cases", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTreatedCase(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("treated_cases", "id = ?", new String[]{String.valueOf(id)});
    }

    public List<TreatedCase> getAllTreatedCases() {
        List<TreatedCase> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM treated_cases ORDER BY id DESC", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new TreatedCase(cursor.getInt(0), cursor.getString(1), 
                            cursor.getString(2), cursor.getString(4), cursor.getString(3)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error loading history: " + e.getMessage());
        }
        return list;
    }

    public Cursor getDangerLevelStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT danger_level, COUNT(*) as count FROM emergencies GROUP BY danger_level", null);
    }

    public void addContact(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        db.insert("custom_contacts", null, values);
    }

    public void updateContact(int id, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        db.update("custom_contacts", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("custom_contacts", "id = ?", new String[]{String.valueOf(id)});
    }
}
