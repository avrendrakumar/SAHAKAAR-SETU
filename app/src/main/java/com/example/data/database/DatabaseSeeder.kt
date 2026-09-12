package com.example.data.database

import com.example.data.model.*

object DatabaseSeeder {

    fun getInitialUsers(): List<UserEntity> {
        val defaultHash = com.example.data.auth.PasswordSecurity.hashPassword("demo123")
        return listOf(
            UserEntity("u_cust_1", "customer@sahakaarsetu.demo", "Rameshwar Prasad", "CUSTOMER", "+91 98350 12345", "Patna", null, "SS-CUS-000001", "Bihar", "Patna", "Kankarbagh", defaultHash, true, true),
            UserEntity("u_cust_2", "anita.verma@example.com", "Anita Verma", "CUSTOMER", "+91 94310 98765", "Muzaffarpur", null, "SS-CUS-000002", "Bihar", "Muzaffarpur", "Juran Chhapra", defaultHash, true, true),
            UserEntity("u_cust_3", "suresh.singh@example.com", "Suresh Kumar Singh", "CUSTOMER", "+91 91220 44556", "Gaya", null, "SS-CUS-000003", "Bihar", "Gaya", "Civil Lines", defaultHash, true, true),
            UserEntity("u_cust_4", "priya.jha@example.com", "Priya Jha", "CUSTOMER", "+91 99340 77889", "Darbhanga", null, "SS-CUS-000004", "Bihar", "Darbhanga", "Laheriasarai", defaultHash, true, true),
            UserEntity("u_cust_5", "vikas.yadav@example.com", "Vikas Yadav", "CUSTOMER", "+91 98355 33221", "Nalanda", null, "SS-CUS-000005", "Bihar", "Nalanda", "Bihar Sharif", defaultHash, true, true),

            // Worker User
            UserEntity("u_work_1", "worker@sahakaarsetu.demo", "Raj Kumar Sharma", "WORKER", "+91 98765 43210", "Patna", "coop_patna", "SS-WRK-000001", "Bihar", "Patna", "Boring Road", defaultHash, true, true),
            UserEntity("u_work_2", "ram.mandal@sahakaarsetu.demo", "Ram Ashray Mandal", "WORKER", "+91 97654 32109", "Patna", "coop_patna", "SS-WRK-000002", "Bihar", "Patna", "Fraser Road", defaultHash, true, true),
            UserEntity("u_work_3", "chandan.mistri@sahakaarsetu.demo", "Chandan Mistri", "WORKER", "+91 96543 21098", "Muzaffarpur", "coop_muz", "SS-WRK-000003", "Bihar", "Muzaffarpur", "Juran Chhapra", defaultHash, true, true),
            UserEntity("u_work_4", "sanjay.paswan@sahakaarsetu.demo", "Sanjay Paswan", "WORKER", "+91 95432 10987", "Gaya", "coop_gaya", "SS-WRK-000004", "Bihar", "Gaya", "Bodhtown", defaultHash, true, true),

            // Cooperative Admins
            UserEntity("u_admin_1", "admin@sahakaarsetu.demo", "Awadhesh Narayan (Secy)", "COOPERATIVE_ADMIN", "+91 94312 00112", "Patna", "coop_patna", "SS-ADM-000001", "Bihar", "Patna", "Kankarbagh", defaultHash, true, true),
            UserEntity("u_admin_2", "muz.admin@sahakaarsetu.demo", "Bhagwan Das", "COOPERATIVE_ADMIN", "+91 94314 00223", "Muzaffarpur", "coop_muz", "SS-ADM-000002", "Bihar", "Muzaffarpur", "Juran Chhapra", defaultHash, true, true),

            // Federation Admin
            UserEntity("u_fed_1", "federation@sahakaarsetu.demo", "Dr. Kameshwar Thakur", "FEDERATION_ADMIN", "+91 94310 00001", "Patna", null, "SS-FED-000001", "Bihar", "Patna", "Patna Main", defaultHash, true, true),

            // Super Admin
            UserEntity("u_super_1", "superadmin@sahakaarsetu.demo", "Sahakaar Setu Directorate", "SUPER_ADMIN", "+91 98350 00000", "Patna", null, "SS-SUP-000001", "Bihar", "Patna", "State Secretariat", defaultHash, true, true)
        )
    }

    fun getInitialCooperatives(): List<CooperativeEntity> = listOf(
        CooperativeEntity("coop_patna", "Patna Shramik Vikas Sahakari Samiti", "COOP-BR-PAT-2018-094", "Patna", "Bihar", "+91 612 2234567", 142, 4.9f, 2018),
        CooperativeEntity("coop_muz", "Muzaffarpur Karigar Swavlamban Sahakari", "COOP-BR-MUZ-2019-112", "Muzaffarpur", "Bihar", "+91 621 2289456", 98, 4.8f, 2019),
        CooperativeEntity("coop_gaya", "Gaya Kaushal Utthan Labour Cooperative", "COOP-BR-GAY-2020-055", "Gaya", "Bihar", "+91 631 2221980", 86, 4.7f, 2020),
        CooperativeEntity("coop_nalanda", "Nalanda Nirman Karigar Sangh Co-op", "COOP-BR-NAL-2021-033", "Nalanda", "Bihar", "+91 611 2254321", 64, 4.9f, 2021),
        CooperativeEntity("coop_darbhanga", "Mithilanchal Shilp & Dakshta Sahakari", "COOP-BR-DAR-2021-078", "Darbhanga", "Bihar", "+91 627 2243210", 72, 4.8f, 2021)
    )

    fun getInitialCategories(): List<ServiceCategoryEntity> = listOf(
        ServiceCategoryEntity("cat_plumb", "Plumbing", "प्लंबिंग (नलसाजी)", "ic_plumbing", 350.0, "Pipe leaks, tap fixing, bathroom fittings & pipeline laying", true),
        ServiceCategoryEntity("cat_elec", "Electrical", "इलेक्ट्रिकल (बिजली मिस्त्री)", "ic_electric", 300.0, "Wiring, MCB tripping, fan, switches, inverter & lighting", true),
        ServiceCategoryEntity("cat_carp", "Carpentry", "बढ़ई काम (कारपेंटर)", "ic_carpenter", 450.0, "Furniture repair, door locks, modular kitchen & woodwork", false),
        ServiceCategoryEntity("cat_paint", "Painting", "पेंटिंग (रंगाई-पुताई)", "ic_paint", 500.0, "Interior, exterior wall painting, waterproof putty & polish", false),
        ServiceCategoryEntity("cat_mason", "Masonry", "राजमिस्त्री (निर्माण कार्य)", "ic_mason", 550.0, "Brickwork, plastering, tile laying, floor repair & cement jobs", false),
        ServiceCategoryEntity("cat_ac", "AC Repair", "एसी रिपेयर एवं सर्विस", "ic_ac", 499.0, "Gas charging, cooling coil servicing, compressor & filter clean", true),
        ServiceCategoryEntity("cat_appliance", "Appliance Repair", "घरेलू उपकरण मरम्मत", "ic_appliance", 350.0, "Washing machine, refrigerator, geyser & microwave repair", true),
        ServiceCategoryEntity("cat_weld", "Welding", "वेल्डिंग एवं फैब्रिकेशन", "ic_weld", 400.0, "Iron gate, railing, shutter, grill & structural fabrication", false),
        ServiceCategoryEntity("cat_clean", "Cleaning", "सफाई एवं स्वच्छता", "ic_clean", 299.0, "Deep home cleaning, water tank cleaning & sanitation", false),
        ServiceCategoryEntity("cat_solar", "Solar Installation", "सोलर पैनल स्थापना", "ic_solar", 600.0, "Solar rooftop panel installation, wiring & battery hookup", false),
        ServiceCategoryEntity("cat_garden", "Gardening", "बागवानी एवं पौधरोपण", "ic_garden", 300.0, "Lawn maintenance, pruning, pest control & plant care", false),
        ServiceCategoryEntity("cat_sec", "Security", "सुरक्षा गार्ड", "ic_sec", 450.0, "Trained cooperative security guards for event or property", true),
        ServiceCategoryEntity("cat_driver", "Driver", "चालक (ड्राइवर)", "ic_driver", 400.0, "Verified heavy & light motor commercial drivers", true),
        ServiceCategoryEntity("cat_labour", "General Labour", "सामान्य श्रमिक (श्रम)", "ic_labour", 350.0, "Loading, unloading, warehouse, event setup & earthwork", false)
    )

    fun getInitialWorkers(): List<WorkerProfileEntity> = listOf(
        WorkerProfileEntity(
            "w_raj", "u_work_1", "SS-PAT-EL-0142", "Raj Kumar Sharma", "+91 98765 43210",
            "coop_patna", "Patna Shramik Vikas Sahakari Samiti", "Electrical",
            "Wiring, Inverter Setup, MCB Repair, Industrial Panel", 7, "VERIFIED",
            "NSDC Level 4 Certified Electrician, Govt. ITI Digha Patna", 4.9f, 214, 214, 98, 450.0,
            isAvailable = true, isBusy = false, isEmergencyReady = true,
            lat = 25.6110, lng = 85.1440, city = "Patna", zone = "Kankarbagh & Boring Road", photoAvatarId = 1
        ),
        WorkerProfileEntity(
            "w_ram", "u_work_2", "SS-PAT-PL-0205", "Ram Ashray Mandal", "+91 97654 32109",
            "coop_patna", "Patna Shramik Vikas Sahakari Samiti", "Plumbing",
            "Leakage Specialist, CPVC Piping, Motor Fitting, Sanitary Ware", 9, "VERIFIED",
            "Skill India Certified Plumber, PHED Approved", 4.85f, 189, 189, 96, 400.0,
            isAvailable = true, isBusy = false, isEmergencyReady = true,
            lat = 25.5941, lng = 85.1376, city = "Patna", zone = "Fraser Road & Gandhi Maidan", photoAvatarId = 2
        ),
        WorkerProfileEntity(
            "w_chandan", "u_work_3", "SS-MUZ-CP-0311", "Chandan Mistri", "+91 96543 21098",
            "coop_muz", "Muzaffarpur Karigar Swavlamban Sahakari", "Carpentry",
            "Modular Kitchen, Door Fitting, Polish, Teakwood Craft", 12, "VERIFIED",
            "National Craftsmanship Certificate, Bihar Skill Mission", 4.92f, 310, 310, 99, 550.0,
            isAvailable = true, isBusy = false, isEmergencyReady = false,
            lat = 26.1209, lng = 85.3647, city = "Muzaffarpur", zone = "Juran Chhapra", photoAvatarId = 3
        ),
        WorkerProfileEntity(
            "w_sanjay", "u_work_4", "SS-GAY-MS-0418", "Sanjay Paswan", "+91 95432 10987",
            "coop_gaya", "Gaya Kaushal Utthan Labour Cooperative", "Masonry",
            "Granite & Tile Laying, Foundation, Plastering, Waterproofing", 8, "VERIFIED",
            "Labour Welfare Board Bihar Certified, Building Craft Diploma", 4.78f, 165, 165, 94, 500.0,
            isAvailable = true, isBusy = false, isEmergencyReady = true,
            lat = 24.7914, lng = 85.0002, city = "Gaya", zone = "Bodhtown & Civil Lines", photoAvatarId = 4
        ),
        WorkerProfileEntity(
            "w_manoj", "u_w_5", "SS-PAT-AC-0520", "Manoj Kumar Bind", "+91 94318 76543",
            "coop_patna", "Patna Shramik Vikas Sahakari Samiti", "AC Repair",
            "Inverter AC, Gas Charge, Deep Foam Cleaning, Duct Repair", 6, "VERIFIED",
            "Voltas Certified Technician, Daikin Authorized Partner", 4.88f, 142, 142, 95, 500.0,
            isAvailable = true, isBusy = false, isEmergencyReady = true,
            lat = 25.6025, lng = 85.1610, city = "Patna", zone = "Rajendra Nagar", photoAvatarId = 5
        ),
        WorkerProfileEntity(
            "w_sunil", "u_w_6", "SS-PAT-PT-0601", "Sunil Vishwakarma", "+91 93344 88776",
            "coop_patna", "Patna Shramik Vikas Sahakari Samiti", "Painting",
            "Texture Paint, Waterproof Putty, Spray Painting, Wood Stain", 10, "VERIFIED",
            "Asian Paints Master Applicator, Co-op Guild Certified", 4.90f, 220, 220, 97, 450.0,
            isAvailable = false, isBusy = true, isEmergencyReady = false,
            lat = 25.6200, lng = 85.1300, city = "Patna", zone = "Danapur", photoAvatarId = 6
        ),
        WorkerProfileEntity(
            "w_deepak", "u_w_7", "SS-NAL-WL-0711", "Deepak Kumar Singh", "+91 92345 67890",
            "coop_nalanda", "Nalanda Nirman Karigar Sangh Co-op", "Welding",
            "Arc Welding, Gas Cutting, Shutter Repair, Heavy Fabrications", 5, "VERIFIED",
            "Govt ITI Bihar Sharif, Safety Standards Level 3", 4.80f, 98, 98, 92, 420.0,
            isAvailable = true, isBusy = false, isEmergencyReady = true,
            lat = 25.1970, lng = 85.5180, city = "Nalanda", zone = "Bihar Sharif Town", photoAvatarId = 7
        ),
        WorkerProfileEntity(
            "w_arun", "u_w_8", "SS-DAR-EL-0822", "Arun Kumar Mishra", "+91 91234 56789",
            "coop_darbhanga", "Mithilanchal Shilp & Dakshta Sahakari", "Electrical",
            "Home Wiring, Transformer Fitting, Submersible Pump Hookup", 8, "VERIFIED",
            "BSEB Wireman License, NSDC Certified", 4.86f, 175, 175, 96, 400.0,
            isAvailable = true, isBusy = false, isEmergencyReady = true,
            lat = 26.1542, lng = 85.8918, city = "Darbhanga", zone = "Laheriasarai", photoAvatarId = 8
        ),
        WorkerProfileEntity(
            "w_vikram", "u_w_9", "SS-PAT-PL-0933", "Vikram Sahni", "+91 90123 45678",
            "coop_patna", "Patna Shramik Vikas Sahakari Samiti", "Plumbing",
            "High Pressure Pumps, Sewer Line Unclogging, Geyser Line", 4, "VERIFIED",
            "Patna Co-op Vocational Centre Diploma", 4.75f, 84, 84, 91, 380.0,
            isAvailable = true, isBusy = false, isEmergencyReady = true,
            lat = 25.6180, lng = 85.0850, city = "Patna", zone = "Bailey Road & Rukanpura", photoAvatarId = 9
        ),
        WorkerProfileEntity(
            "w_ajay", "u_w_10", "SS-PAT-EL-1044", "Ajay Pandit", "+91 89012 34567",
            "coop_patna", "Patna Shramik Vikas Sahakari Samiti", "Electrical",
            "Appliance Installation, Solar Rooftop, Smart Lighting", 3, "PENDING",
            "Apprenticeship Completed, Verification in Progress", 4.60f, 32, 32, 88, 350.0,
            isAvailable = true, isBusy = false, isEmergencyReady = false,
            lat = 25.5900, lng = 85.1800, city = "Patna", zone = "Patna City / Chowk", photoAvatarId = 10
        )
    )

    fun getInitialWelfareWallets(): List<WelfareWalletEntity> = listOf(
        WelfareWalletEntity("w_raj", balance = 4250.0, insuranceCovered = "PM Jeevan Jyoti & Suraksha - ₹4,00,000", trainingCredits = 150, emergencyAssistanceFund = 10000.0, totalContributions = 5600.0),
        WelfareWalletEntity("w_ram", balance = 3800.0, insuranceCovered = "PM Suraksha Bima Yojana - ₹2,00,000", trainingCredits = 120, emergencyAssistanceFund = 8000.0, totalContributions = 4900.0),
        WelfareWalletEntity("w_chandan", balance = 6100.0, insuranceCovered = "Artisan Welfare Group Mediclaim - ₹3,00,000", trainingCredits = 200, emergencyAssistanceFund = 15000.0, totalContributions = 7800.0),
        WelfareWalletEntity("w_sanjay", balance = 3200.0, insuranceCovered = "BOCW Board Bihar Accidental Shield", trainingCredits = 90, emergencyAssistanceFund = 6000.0, totalContributions = 4100.0)
    )

    fun getInitialBookings(): List<BookingEntity> = listOf(
        BookingEntity(
            id = "bk_001",
            bookingNumber = "BK-2026-091",
            customerId = "u_cust_1",
            customerName = "Rameshwar Prasad",
            customerPhone = "+91 98350 12345",
            workerId = "w_raj",
            workerName = "Raj Kumar Sharma",
            cooperativeId = "coop_patna",
            serviceCategory = "Electrical",
            problemDescription = "Main circuit breaker keeps tripping when heavy appliance is turned on. Need full board checkup.",
            urgency = "HIGH",
            isEmergency = false,
            locationAddress = "Plot 42, Anandpuri, Boring Road",
            city = "Patna",
            scheduledDate = "Today",
            scheduledTime = "02:30 PM",
            status = "ASSIGNED",
            laborCost = 450.0,
            materialCost = 80.0,
            platformFee = 25.0,
            totalAmount = 555.0,
            paymentStatus = "PENDING",
            paymentMethod = null
        ),
        BookingEntity(
            id = "bk_002",
            bookingNumber = "BK-2026-088",
            customerId = "u_cust_1",
            customerName = "Rameshwar Prasad",
            customerPhone = "+91 98350 12345",
            workerId = "w_ram",
            workerName = "Ram Ashray Mandal",
            cooperativeId = "coop_patna",
            serviceCategory = "Plumbing",
            problemDescription = "Underground tank water inlet valve burst. Water leaking heavily into courtyard.",
            urgency = "EMERGENCY",
            isEmergency = true,
            locationAddress = "Road No 3, Kankarbagh Colony",
            city = "Patna",
            scheduledDate = "Yesterday",
            scheduledTime = "11:00 AM",
            status = "COMPLETED",
            laborCost = 500.0,
            materialCost = 150.0,
            platformFee = 25.0,
            totalAmount = 675.0,
            paymentStatus = "PAID",
            paymentMethod = "UPI",
            completedAt = System.currentTimeMillis() - 86400000
        ),
        BookingEntity(
            id = "bk_003",
            bookingNumber = "BK-2026-095",
            customerId = "u_cust_2",
            customerName = "Anita Verma",
            customerPhone = "+91 94310 98765",
            workerId = "w_chandan",
            workerName = "Chandan Mistri",
            cooperativeId = "coop_muz",
            serviceCategory = "Carpentry",
            problemDescription = "Teakwood dining table leg detached and polish faded. Need sturdy restoration.",
            urgency = "NORMAL",
            isEmergency = false,
            locationAddress = "Behind Mithila University Campus",
            city = "Muzaffarpur",
            scheduledDate = "Tomorrow",
            scheduledTime = "10:00 AM",
            status = "ASSIGNED",
            laborCost = 600.0,
            materialCost = 120.0,
            platformFee = 25.0,
            totalAmount = 745.0,
            paymentStatus = "PENDING",
            paymentMethod = null
        ),
        BookingEntity(
            id = "bk_004",
            bookingNumber = "BK-2026-099",
            customerId = "u_cust_3",
            customerName = "Suresh Kumar Singh",
            customerPhone = "+91 91220 44556",
            workerId = "w_sanjay",
            workerName = "Sanjay Paswan",
            cooperativeId = "coop_gaya",
            serviceCategory = "Masonry",
            problemDescription = "Wall tile dampness in bathroom requiring re-grouting and cement sealant.",
            urgency = "NORMAL",
            isEmergency = false,
            locationAddress = "Station Road, Gaya",
            city = "Gaya",
            scheduledDate = "Today",
            scheduledTime = "04:00 PM",
            status = "WORK_STARTED",
            laborCost = 550.0,
            materialCost = 200.0,
            platformFee = 25.0,
            totalAmount = 775.0,
            paymentStatus = "PENDING",
            paymentMethod = null
        )
    )

    fun getInitialInvoices(): List<InvoiceEntity> = listOf(
        InvoiceEntity(
            id = "inv_002",
            invoiceNumber = "INV-SS-2026-088",
            bookingId = "bk_002",
            customerName = "Rameshwar Prasad",
            workerName = "Ram Ashray Mandal (Verified Plumber)",
            cooperativeName = "Patna Shramik Vikas Sahakari Samiti",
            serviceName = "Emergency Plumbing Pipeline Repair",
            laborCost = 500.0,
            materialCost = 150.0,
            platformFee = 25.0,
            totalAmount = 675.0,
            paymentStatus = "PAID (UPI)",
            paymentMethod = "UPI (BHIM / GooglePay)",
            issuedDate = "02 Sep 2026"
        )
    )

    fun getInitialDemandForecasts(): List<DemandForecastEntity> = listOf(
        DemandForecastEntity("df_1", "Plumbing", "Patna (Zone 3 - Kankarbagh)", 145, 92, 53, "High post-monsoon pipe repair surge. Reallocate 25 plumbers from Danapur Zone."),
        DemandForecastEntity("df_2", "Electrical", "Muzaffarpur (Town Center)", 180, 140, 40, "Festival lighting demand expected. Enroll 15 certified apprentices."),
        DemandForecastEntity("df_3", "AC Repair", "Patna (Boring Road & Rajendra Nagar)", 210, 115, 95, "Seasonal peak temperature. Cooperative overtime bonus active."),
        DemandForecastEntity("df_4", "Masonry & Tiling", "Gaya (Civil Lines)", 95, 75, 20, "Pre-Chhath renovation wave. Steady cooperative member allocation."),
        DemandForecastEntity("df_5", "Painting", "Nalanda (Bihar Sharif)", 120, 85, 35, "Commercial whitewashing projects. Mobilize 3 painting squads.")
    )

    fun getInitialInstitutionalBookings(): List<InstitutionalBookingEntity> = listOf(
        InstitutionalBookingEntity(
            id = "inst_001",
            institutionName = "Patna Central Model School",
            institutionType = "SCHOOL",
            requiredSkill = "Electrical",
            workerCount = 10,
            durationDays = 3,
            locationCity = "Patna",
            contactPerson = "Er. Vinod Kashyap (Facilities Head)",
            contactPhone = "+91 94311 88990",
            status = "ALLOCATED",
            cooperativeId = "coop_patna",
            estimatedBudget = 18500.0
        ),
        InstitutionalBookingEntity(
            id = "inst_002",
            institutionName = "AIIMS Residential Staff Quarters",
            institutionType = "HOSPITAL",
            requiredSkill = "Plumbing",
            workerCount = 8,
            durationDays = 5,
            locationCity = "Patna",
            contactPerson = "Dr. Anita Shekhar (Admin In-charge)",
            contactPhone = "+91 94312 77665",
            status = "PENDING_ALLOCATION",
            cooperativeId = "coop_patna",
            estimatedBudget = 24000.0
        ),
        InstitutionalBookingEntity(
            id = "inst_003",
            institutionName = "Mithila Heritage Hotel & Convention",
            institutionType = "HOTEL",
            requiredSkill = "Carpentry",
            workerCount = 6,
            durationDays = 4,
            locationCity = "Darbhanga",
            contactPerson = "Rajiv Nandan (Manager)",
            contactPhone = "+91 98351 22334",
            status = "IN_PROGRESS",
            cooperativeId = "coop_darbhanga",
            estimatedBudget = 16200.0
        )
    )

    fun getInitialProfessions(): List<ProfessionEntity> = listOf(
        ProfessionEntity("prof_elec", "Electrician", "इलेक्ट्रीशियन (बिजली मिस्त्री)", "ic_electric", true),
        ProfessionEntity("prof_plumb", "Plumber", "प्लंबर (नलसाजी)", "ic_plumbing", true),
        ProfessionEntity("prof_carp", "Carpenter", "बढ़ई (कारपेंटर)", "ic_carpenter", true),
        ProfessionEntity("prof_mason", "Mason", "राजमिस्त्री (भवन निर्माण)", "ic_mason", true),
        ProfessionEntity("prof_paint", "Painter", "पेंटर (रंगाई-पुताई)", "ic_paint", true),
        ProfessionEntity("prof_weld", "Welder", "वेल्डर (लोहा एवं ग्रिल)", "ic_weld", true),
        ProfessionEntity("prof_mech", "Mechanic", "मैकेनिक (वाहन एवं मोटर)", "ic_mechanic", true),
        ProfessionEntity("prof_driver", "Driver", "चालक (ड्राइवर)", "ic_driver", true),
        ProfessionEntity("prof_clean", "Cleaner", "सफाईकर्मी (स्वच्छता)", "ic_clean", true),
        ProfessionEntity("prof_agri", "Agricultural Worker", "कृषि श्रमिक (खेती कार्य)", "ic_agri", true),
        ProfessionEntity("prof_const", "Construction Worker", "निर्माण श्रमिक (मजदूरी)", "ic_labour", true),
        ProfessionEntity("prof_tailor", "Tailor", "दर्जी (सिलाई कार्य)", "ic_tailor", true),
        ProfessionEntity("prof_tech", "Technician", "तकनीशियन (घरेलू उपकरण)", "ic_tech", true),
        ProfessionEntity("prof_other", "Other Artisan", "अन्य कुशल कारीगर", "ic_skill", true)
    )

    fun getInitialNotifications(): List<NotificationEntity> = listOf(
        NotificationEntity(
            id = "notif_1",
            userId = "u_cust_1",
            role = "CUSTOMER",
            title = "Worker Assigned",
            message = "Ram Ashray Mandal (Verified Plumber) is assigned to your booking #BK-2026-092.",
            isRead = false,
            actionType = "BOOKING_UPDATE"
        ),
        NotificationEntity(
            id = "notif_2",
            userId = "u_work_1",
            role = "WORKER",
            title = "New Hiring Request",
            message = "New urgent electrical booking request received from Boring Road, Patna.",
            isRead = false,
            actionType = "NEW_JOB"
        ),
        NotificationEntity(
            id = "notif_3",
            userId = "u_cust_1",
            role = "CUSTOMER",
            title = "Cooperative Welfare Discount",
            message = "Your invoice #INV-SS-2026-088 was cleared with 0% extra commission via UPI.",
            isRead = true,
            actionType = "PAYMENT"
        )
    )

    fun getInitialLocations(): List<LocationEntity> = listOf(
        // Patna District
        LocationEntity("loc_pat_1", "Bihar", "Patna", "Patna Central", "Boring Road / Anandpuri", "800001", 25.6154, 85.1240),
        LocationEntity("loc_pat_2", "Bihar", "Patna", "Patna City", "Kankarbagh Colony", "800020", 25.5902, 85.1583),
        LocationEntity("loc_pat_3", "Bihar", "Patna", "Danapur", "Danapur Cantonment / Khagaul", "801503", 25.6324, 85.0450),
        LocationEntity("loc_pat_4", "Bihar", "Patna", "Phulwari Sharif", "AIIMS Road / Walmi", "801505", 25.5721, 85.0812),
        LocationEntity("loc_pat_5", "Bihar", "Patna", "Rajendra Nagar", "Kadam Kuan / Bazar Samiti", "800016", 25.6025, 85.1612),
        LocationEntity("loc_pat_6", "Bihar", "Patna", "Bihta", "Bihta IIT Campus Area / Kanhauli", "801103", 25.5562, 84.8720),
        LocationEntity("loc_pat_7", "Bihar", "Patna", "Fatuha", "Fatuha Industrial Area / Kachchi Dargah", "803201", 25.5123, 85.3120),
        LocationEntity("loc_pat_8", "Bihar", "Patna", "Bakhtiyarpur", "Salimpur / Champa Nagar", "803212", 25.4612, 85.5284),
        LocationEntity("loc_pat_9", "Bihar", "Patna", "Digha", "Digha Ghat / Ashiana Nagar", "800011", 25.6420, 85.1012),
        LocationEntity("loc_pat_10", "Bihar", "Patna", "Anisabad", "Gardanibagh / Chitkohra", "800002", 25.5810, 85.1120),

        // Gaya District
        LocationEntity("loc_gay_1", "Bihar", "Gaya", "Gaya Town", "Civil Lines / Rampur", "823001", 24.7964, 85.0039),
        LocationEntity("loc_gay_2", "Bihar", "Gaya", "Bodh Gaya", "Mahabodhi Temple Area / Mastipur", "824231", 24.6951, 84.9913),
        LocationEntity("loc_gay_3", "Bihar", "Gaya", "Manpur", "Buniyadganj / Weavers Colony", "823003", 24.8021, 85.0210),
        LocationEntity("loc_gay_4", "Bihar", "Gaya", "Tekari", "Tekari Fort / Rani Ganj", "824236", 24.9312, 84.8321),
        LocationEntity("loc_gay_5", "Bihar", "Gaya", "Sherghati", "Hamzapur / Chhatarpur", "824211", 24.5712, 84.7891),

        // Muzaffarpur District
        LocationEntity("loc_muz_1", "Bihar", "Muzaffarpur", "Muzaffarpur Town", "Club Road / Mithanpura", "842001", 26.1209, 85.3647),
        LocationEntity("loc_muz_2", "Bihar", "Muzaffarpur", "Motipur", "Baruraj / Sugar Mill Compound", "843111", 26.2412, 85.1720),
        LocationEntity("loc_muz_3", "Bihar", "Muzaffarpur", "Kanti", "Thermal Power Nagar / Damodarpur", "843109", 26.1912, 85.3012),
        LocationEntity("loc_muz_4", "Bihar", "Muzaffarpur", "Sakra", "Dholi Pusa Road / Subhai", "843105", 26.0412, 85.5012),
        LocationEntity("loc_muz_5", "Bihar", "Muzaffarpur", "Saraiya", "Jaitpur / Paroo Road", "843126", 26.0212, 85.1612),

        // Nalanda District
        LocationEntity("loc_nal_1", "Bihar", "Nalanda", "Bihar Sharif", "Khandakpar / Ramchandrapur", "803101", 25.1983, 85.5149),
        LocationEntity("loc_nal_2", "Bihar", "Nalanda", "Rajgir", "Kund Area / Venuvan Nagar", "803116", 25.0284, 85.4214),
        LocationEntity("loc_nal_3", "Bihar", "Nalanda", "Hilsa", "Yogipur / Station Road", "801302", 25.3184, 85.2812),
        LocationEntity("loc_nal_4", "Bihar", "Nalanda", "Islampur", "Bardih / Bazar Samiti", "801303", 25.1412, 85.2012),

        // Darbhanga District
        LocationEntity("loc_dar_1", "Bihar", "Darbhanga", "Darbhanga Town", "Laheriasarai / Tower Chowk", "846001", 26.1542, 85.8918),
        LocationEntity("loc_dar_2", "Bihar", "Darbhanga", "Benipur", "Bahera / Ashapur Chowk", "847103", 26.1212, 86.1312),
        LocationEntity("loc_dar_3", "Bihar", "Darbhanga", "Keoti", "Keoti Ranway / Pachrukhi", "847121", 26.2612, 85.9312),

        // Bhagalpur District
        LocationEntity("loc_bha_1", "Bihar", "Bhagalpur", "Bhagalpur Town", "Tilkamanjhi / Adampur", "812001", 25.2425, 86.9842),
        LocationEntity("loc_bha_2", "Bihar", "Bhagalpur", "Kahalgaon", "NTPC Township / Bateshwar Asthan", "813203", 25.2712, 87.2312),
        LocationEntity("loc_bha_3", "Bihar", "Bhagalpur", "Sultanganj", "Ajgaibinath / Station Bazar", "813213", 25.2412, 86.7312),
        LocationEntity("loc_bha_4", "Bihar", "Bhagalpur", "Naugachia", "Tetri / Railway Colony", "853204", 25.3912, 87.1012),

        // Begusarai District
        LocationEntity("loc_beg_1", "Bihar", "Begusarai", "Begusarai Town", "Har Har Mahadev Chowk / Refinery Area", "851101", 25.4182, 86.1272),
        LocationEntity("loc_beg_2", "Bihar", "Begusarai", "Barauni", "Barauni Urvarak Nagar / Garhara", "851112", 25.4612, 85.9812),
        LocationEntity("loc_beg_3", "Bihar", "Begusarai", "Teghra", "Bazar Chowk / Madhurapur", "851133", 25.4812, 85.8912),

        // Samastipur District
        LocationEntity("loc_sam_1", "Bihar", "Samastipur", "Samastipur Town", "Magardahi Ghat / Mohanpur", "848101", 25.8628, 85.7811),
        LocationEntity("loc_sam_2", "Bihar", "Samastipur", "Dalsinghsarai", "Maltoli / Railway Colony", "848114", 25.6612, 85.8312),
        LocationEntity("loc_sam_3", "Bihar", "Samastipur", "Pusa", "Dr. Rajendra Prasad Central Agri Univ / Harpur", "848125", 25.9812, 85.6712),

        // Vaishali District
        LocationEntity("loc_vai_1", "Bihar", "Vaishali", "Hajipur", "Anwarpur / Paswan Chowk", "844101", 25.6858, 85.2146),
        LocationEntity("loc_vai_2", "Bihar", "Vaishali", "Mahua", "Mahua Bazar / Singhara", "844122", 25.8212, 85.3912),
        LocationEntity("loc_vai_3", "Bihar", "Vaishali", "Lalganj", "Basanta / Gandak Ghat", "844121", 25.8712, 85.1812),

        // Purnia District
        LocationEntity("loc_pur_1", "Bihar", "Purnia", "Purnia Town", "Bhatta Bazar / Line Bazar", "854301", 25.7771, 87.4753),
        LocationEntity("loc_pur_2", "Bihar", "Purnia", "Banmankhi", "Sugar Factory Colony / Rasalganj", "854202", 25.9012, 87.1612),
        LocationEntity("loc_pur_3", "Bihar", "Purnia", "Kasba", "Gadhbanaili / Mill Bazar", "854330", 25.8512, 87.5312)
    )
}
