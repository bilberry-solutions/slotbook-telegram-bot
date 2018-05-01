package me.slotbook.client.telegram.model.slotbook

import com.osinka.i18n.Lang

object Language {
  val defaultLangCode = "en"

  val default = Lang(defaultLangCode)
}

case class Language(lang: Lang) {
  def code: String = this.lang.locale.getLanguage
}
