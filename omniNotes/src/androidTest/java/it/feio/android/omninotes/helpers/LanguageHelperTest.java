/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.helpers;

import android.content.SharedPreferences;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;

import java.util.Locale;


public class LanguageHelperTest extends BaseAndroidTestCase {

	public void testShouldChangeSharedPrefrencesLanguage() {
		LanguageHelper.updateLanguage(testContext, Locale.ITALY.toString());
		SharedPreferences prefs = testContext.getSharedPreferences(Constants.PREFS_NAME, testContext
				.MODE_MULTI_PROCESS);
		String language = prefs.getString(Constants.PREF_LANG, "");
		assertEquals(Locale.ITALY.toString(), language);
	}

	public void testShouldChangeAppLanguage() {
		LanguageHelper.updateLanguage(testContext, Locale.ITALY.toString());
		assertTranslationMatches(Locale.ITALY.toString(), R.string.add_note);
	}

	public void testSameStaticStringToEnsureTranslationsAreCorrect() {
		assertTranslationMatches("ar_SA", R.string.add_note, "إضافة نقطة");
		assertTranslationMatches("es_XA", R.string.add_note, "Amestar Nota");
		assertTranslationMatches("ca_ES", R.string.add_note, "Afegeix una nota");
		assertTranslationMatches("zh_CN", R.string.add_note, "添加记事");
		assertTranslationMatches("zh_TW", R.string.add_note, "新增筆記");
		assertTranslationMatches("hr_HR", R.string.add_note, "Dodaj Bilješku");
		assertTranslationMatches("cs_CZ", R.string.add_note, "Přidat poznámku");
		assertTranslationMatches("nl_NL", R.string.add_note, "Notitie toevoegen");
		assertTranslationMatches("en_US", R.string.add_note, "Add Note");
		assertTranslationMatches("fr_FR", R.string.add_note, "Ajouter une note");
		assertTranslationMatches("km_KH", R.string.add_note, "បង្កើត");
		assertTranslationMatches("de_DE", R.string.add_note, "Notiz hinzufügen");
		assertTranslationMatches("gl_ES", R.string.add_note, "Engadir nota");
		assertTranslationMatches("el_GR", R.string.add_note, "Προσθήκη σημείωσης");
		assertTranslationMatches("iw_IL", R.string.add_note, "הוספת הערה");
		assertTranslationMatches("hi_IN", R.string.add_note, "नोट जोड़ें");
		assertTranslationMatches("hu_HU", R.string.add_note, "Jegyzet hozzáadása");
		assertTranslationMatches("in_ID", R.string.add_note, "Tambahkan Catatan");
		assertTranslationMatches("it_IT", R.string.add_note, "Aggiungi nota");
		assertTranslationMatches("ja_JP", R.string.add_note, "ノートを追加");
		assertTranslationMatches("lo_LA", R.string.add_note, "ເພີ່ມບັນທຶກ");
		assertTranslationMatches("lv_LV", R.string.add_note, "Pievienot piezīmi");
		assertTranslationMatches("pl_PL", R.string.add_note, "Dodaj Notatkę");
		assertTranslationMatches("pt_BR", R.string.add_note, "Adicionar nota");
		assertTranslationMatches("pt_PT", R.string.add_note, "Adicionar nota");
		assertTranslationMatches("ru_RU", R.string.add_note, "Создать заметку");
		assertTranslationMatches("sr_SP", R.string.add_note, "Додај белешку");
		assertTranslationMatches("sk_SK", R.string.add_note, "Pridať poznámku");
		assertTranslationMatches("sl_SI", R.string.add_note, "Dodaj beležko");
		assertTranslationMatches("es_ES", R.string.add_note, "Añadir Nota");
		assertTranslationMatches("sv_SE", R.string.add_note, "Lägg till Anteckning");
		assertTranslationMatches("tr_TR", R.string.add_note, "Not Ekle");
		assertTranslationMatches("uk_UA", R.string.add_note, "Додати нотатку");
	}

	private void assertTranslationMatches(String locale, int resourceId) {
		assertTranslationMatches(locale, resourceId, testContext.getString(resourceId));
	}

	private void assertTranslationMatches(String locale, int resourceId, String string) {
		assertEquals(LanguageHelper.getLocalizedString(testContext, locale, resourceId), string);
	}
}
