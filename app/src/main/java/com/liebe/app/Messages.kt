package com.liebe.app

/** Sammlung der Liebestexte. {n} wird durch den Namen ersetzt. Einfach erweitern oder ändern! */
object Messages {
    private val all = listOf(
        "Ich liebe dich, {n}. Jeden Tag ein bisschen mehr. ❤️",
        "Danke, dass es dich gibt. Du machst mein Leben heller. ❤️",
        "Mit dir fühlt sich jeder Moment wie Zuhause an.",
        "Ich bin so dankbar, dass ich dich an meiner Seite habe.",
        "Du bist mein liebster Mensch auf der ganzen Welt. ❤️",
        "Deine Nähe ist mein Lieblingsplatz.",
        "Danke für dein Lächeln, das jeden Tag schöner macht.",
        "Ich liebe dich mehr, als Worte sagen können.",
        "Du bist das Beste, was mir je passiert ist. ❤️",
        "Mit dir wird sogar der Alltag zum Abenteuer.",
        "Danke für deine Geduld, deine Wärme und deine Liebe.",
        "Egal wohin das Leben uns führt – ich will es mit dir gehen.",
        "Du bist mein Herz, mein Halt und mein Zuhause. ❤️",
        "Ich bin unendlich dankbar für jede Minute mit dir.",
        "Du bringst Farbe in mein Leben – und nicht nur in deine Haare. 😉❤️",
        "Ich liebe alles an dir, {n} – auch deine kleinen Macken.",
        "Mit dir an meiner Seite habe ich alles, was ich brauche.",
        "Danke, dass du genau so bist, wie du bist.",
        "Du bist mein Sonnenschein an jedem Tag. ☀️❤️",
        "Jeder Tag mit dir ist ein Geschenk. Danke dafür.",
        "Ich liebe dich – heute, morgen und für immer. ❤️",
        "Du machst mich zu einem glücklicheren Menschen.",
        "Danke für all die kleinen und großen Momente mit dir.",
        "Mein Herz gehört dir, {n}. Jetzt und immer. ❤️"
    )

    /** Feste Texte zu den eingebauten Bildern (photos/01.jpg ... 20.jpg), in dieser Reihenfolge. */
    val photoTexts = listOf(
        "Selbst mit zugekniffenen Augen in der Sonne bist du das Schönste, was ich kenne. Ich liebe dich, {n}. ❤️",
        "Dein Lächeln bringt mein Herz jedes Mal zum Stolpern. Danke, dass es dich gibt. ❤️",
        "Dein Lachen ist mein Lieblingsgeräusch. Mit dir schmeckt sogar das Eis besser. 🍦❤️",
        "Mit dir fühlt sich jeder ruhige Moment wie das größte Glück an. Ich liebe dich.",
        "Du bist wunderschön, genau so, wie du bist. Danke, dass du mein Leben so bunt machst. ❤️",
        "Wenn du so lachst, vergesse ich alles um mich herum. Ich bin so dankbar für dich. ☀️❤️",
        "Dein kleines Lächeln wärmt mein Herz mehr als alles andere. Ich liebe dich von ganzem Herzen.",
        "Dein Schmunzeln und deine Sommersprossen: Ich liebe jedes Detail an dir. ❤️",
        "Du bringst mich zum Lachen wie kein anderer Mensch. Danke für deine Verrücktheit und deine Liebe. 😄❤️",
        "Mit dir ist selbst ein ganz normaler Tag auf dem Balkon ein kleines Paradies. Danke dafür. 🌴❤️",
        "Dein Gesicht ist das Letzte, was ich vor dem Einschlafen sehen will. Gute Nacht, {n}. 🌙❤️",
        "Jeder Kuss mit dir fühlt sich an wie das erste Mal. Ich liebe dich, jetzt und für immer. ❤️",
        "Dein Lächeln strahlt heller als die Sonne durch die Blätter. Danke, dass du mein Licht bist. 🌳❤️",
        "Du gibst mir mehr Energie als jede Dose. Danke für alles, was du für mich bist. ❤️",
        "Selbst wenn du mir nur zuwinkst, schlägt mein Herz schneller. Ich liebe dich. 👋❤️",
        "Ich könnte dich stundenlang einfach nur anschauen. Danke für jeden Tag mit dir. ❤️",
        "Dein Lachen steckt an und macht jeden Tag leichter. Ich bin so froh, dass ich dich habe.",
        "Du und dein Stitch: Mein Herz schmilzt bei diesem Anblick. Ich liebe dich, {n}. 💙❤️",
        "Eingekuschelt neben dir ist mein liebster Platz auf der Welt. Danke für deine Wärme. ❤️",
        "Du siehst toll aus, und innen bist du noch schöner. Ich bin so stolz und dankbar, dass du meine Freundin bist. ❤️"
    )

    fun fill(text: String, name: String) = text.replace("{n}", name.trim().ifEmpty { "mein Schatz" })

    private val pool = ArrayDeque<String>()

    fun next(name: String): String {
        if (pool.isEmpty()) pool.addAll(all.shuffled())
        return pool.removeFirst().replace("{n}", name.trim().ifEmpty { "mein Schatz" })
    }
}
