package uk.chinnidiwakar

object QuoteRepository {
    private val quotes = listOf(
        "Focus on the step you are taking, not the mountain.",
        "Recovery is not a race; it's a marathon.",
        "Your future self will thank you for today's strength.",
        "One day at a time, one moment at a time.",
        "Mistakes are proof that you are trying.",
        "Fall seven times, stand up eight.",
        "The goal isn't to be perfect, it's to be better than yesterday.",
        "Don't judge each day by the harvest you reap, but by the seeds you plant.",
        "Success is the sum of small efforts, repeated day in and day out.",
        "It does not matter how slowly you go as long as you do not stop.",
        "Believe you can and you're halfway there.",
        "Courage is going on when you don't have strength.",
        "Small progress is still progress.",
        "Your past does not define your future.",
        "Every sunset is an opportunity to reset.",
        "Stay strong, your journey is worth it.",
        "The only person you should try to be better than is who you were yesterday.",
        "Great things never come from comfort zones.",
        "Difficult roads lead to beautiful destinations.",
        "The secret of your future is hidden in your daily routine.",
        "Don't decrease the goal. Increase the effort.",
        "You are stronger than you think.",
        "Character is how you treat yourself when no one is looking.",
        "A year from now, you will wish you had started today.",
        "Discipline is choosing between what you want now and what you want most.",
        "Be patient with yourself. Self-growth is tender.",
        "Everything you've ever wanted is on the other side of fear.",
        "Don't stop until you're proud.",
        "Make your recovery your top priority.",
        "One moment of patience in a moment of anger saves a thousand moments of regret.",
        "You don't have to see the whole staircase, just take the first step."
    )

    fun getQuoteForToday(): String {
        // Uses the day of the year to pick a consistent quote for the whole day
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        return quotes[dayOfYear % quotes.size]
    }
}