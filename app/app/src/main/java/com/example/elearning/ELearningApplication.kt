fun Navigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    courseViewModel: CourseViewModel,
    noteViewModel: NoteViewModel,
    subscriptionManager: SubscriptionManager,
    creditCardViewModel: CreditCardViewModel
) 

creditCardViewModel.loadCreditCards(userId) 