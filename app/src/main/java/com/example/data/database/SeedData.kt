package com.example.data.database

import com.example.data.model.*

object SeedData {
    val cooperatives = listOf(
        CooperativeEntity(
            id = 1,
            name = "Patna Shramik Sahakari Samiti",
            registrationNumber = "COOP-BR-PAT-2018-0042",
            district = "Patna",
            phone = "+91 612 223 4567",
            email = "patna@sahakaarsetu.coop",
            address = "Fraser Road, Near Dak Bungalow, Patna, Bihar 800001",
            rating = 4.9f,
            latitude = 25.6093,
            longitude = 85.1376
        ),
        CooperativeEntity(
            id = 2,
            name = "Muzaffarpur Karigar Sahakari",
            registrationNumber = "COOP-BR-MUZ-2019-0118",
            district = "Muzaffarpur",
            phone = "+91 621 224 8899",
            email = "muzaffarpur@sahakaarsetu.coop",
            address = "Station Road, Sutapatti, Muzaffarpur, Bihar 842001",
            rating = 4.8f,
            latitude = 26.1209,
            longitude = 85.3647
        ),
        CooperativeEntity(
            id = 3,
            name = "Gaya Nirman Sahakari Samiti",
            registrationNumber = "COOP-BR-GAY-2020-0089",
            district = "Gaya",
            phone = "+91 631 222 1144",
            email = "gaya@sahakaarsetu.coop",
            address = "GB Road, Civil Lines, Gaya, Bihar 823001",
            rating = 4.7f,
            latitude = 24.7914,
            longitude = 85.0002
        ),
        CooperativeEntity(
            id = 4,
            name = "Motihari Hunar Sahakari",
            registrationNumber = "COOP-BR-MOT-2021-0205",
            district = "Motihari",
            phone = "+91 6252 232 455",
            email = "motihari@sahakaarsetu.coop",
            address = "Main Market, Chhatauni, Motihari, East Champaran, Bihar 845401",
            rating = 4.8f,
            latitude = 26.6469,
            longitude = 84.9089
        ),
        CooperativeEntity(
            id = 5,
            name = "Vaishali Kushal Sahakari Samiti",
            registrationNumber = "COOP-BR-VAI-2021-0312",
            district = "Vaishali",
            phone = "+91 6224 271 234",
            email = "vaishali@sahakaarsetu.coop",
            address = "Cinema Road, Hajipur, Vaishali, Bihar 844101",
            rating = 4.9f,
            latitude = 25.6858,
            longitude = 85.2152
        )
    )

    val serviceCategories = listOf(
        ServiceCategoryEntity(1, "Plumbing", "नलसाजी एवं पाइपलाइन", "plumbing", 350.0, "Pipe leaks, tap fixtures, sanitary fitting, pump installation", true),
        ServiceCategoryEntity(2, "Electrical", "विद्युत एवं वायरिंग", "electrical_services", 300.0, "Wiring repair, switchboard, MCB tripping, fan installation, inverter setup", true),
        ServiceCategoryEntity(3, "Carpentry", "बढ़ईगीरी एवं फर्नीचर", "carpenter", 400.0, "Furniture repair, door locks, modular kitchen assembly, hinge fixing", false),
        ServiceCategoryEntity(4, "Painting", "रंगाई एवं पुट्टी", "format_paint", 450.0, "Interior, exterior wall painting, waterproof primer, polish, texture work", false),
        ServiceCategoryEntity(5, "Masonry", "राजमिस्त्री एवं निर्माण", "construction", 500.0, "Brickwork, plastering, tile laying, floor leveling, concrete repair", false),
        ServiceCategoryEntity(6, "Welding", "वेल्डिंग एवं फैब्रिकेशन", "hardware", 450.0, "Iron gate, window grill repair, railing welding, metal structure", true),
        ServiceCategoryEntity(7, "AC Repair", "एसी सर्विसिंग एवं मरम्मत", "ac_unit", 550.0, "Split/Window AC servicing, gas refill, compressor issue, cooling check", true),
        ServiceCategoryEntity(8, "Appliance Repair", "घरेलू उपकरण मरम्मत", "microwave", 350.0, "Washing machine, refrigerator, microwave, water purifier RO", true),
        ServiceCategoryEntity(9, "Cleaning", "गहन सफाई सेवाएं", "cleaning_services", 400.0, "Home deep cleaning, water tank sanitization, bathroom scrubbing", false),
        ServiceCategoryEntity(10, "Construction Labour", "निर्माण श्रमिक", "engineering", 450.0, "Material handling, excavation, slab casting, renovation assistance", false),
        ServiceCategoryEntity(11, "Gardening", "बागवानी एवं भूदृश्य", "yard", 300.0, "Lawn maintenance, tree pruning, soil prep, terrace garden maintenance", false),
        ServiceCategoryEntity(12, "Security Guard", "सुरक्षा गार्ड", "security", 600.0, "Trained gatekeeper, premises monitoring, event safety guard", true),
        ServiceCategoryEntity(13, "Driver", "चालक सेवा", "directions_car", 500.0, "Commercial, private vehicle driving, outstation & local transit", false),
        ServiceCategoryEntity(14, "General Labour", "सामान्य श्रम", "handyman", 300.0, "Loading, shifting, warehouse lifting, packaging, odd household jobs", false),
        ServiceCategoryEntity(15, "Solar Installation", "सौर ऊर्जा एवं पैनल", "solar_power", 650.0, "Rooftop solar panel mount, battery inverter wiring, maintenance", true)
    )

    fun generateWorkers(): List<WorkerProfileEntity> {
        val list = mutableListOf<WorkerProfileEntity>()
        val firstNames = listOf(
            "Raj", "Amit", "Suresh", "Manoj", "Vikram", "Ramesh", "Dharmendra", "Sunil", "Pankaj", "Santosh",
            "Anil", "Deepak", "Rakesh", "Arun", "Jitendra", "Mukesh", "Pradeep", "Ashok", "Sanjay", "Vinod",
            "Gopal", "Mahesh", "Kamlesh", "Satendra", "Dinesh", "Ajay", "Mohan", "Chandan", "Shailesh", "Harish",
            "Ravindra", "Sudhir", "Brijesh", "Navin", "Upendra", "Surendra", "Akhilesh", "Lalit", "Shambhu", "Kailash",
            "Kishore", "Subhash", "Nand", "Ram", "Kundan", "Prem", "Tarun", "Manish", "Gautam", "Devendra"
        )
        val lastNames = listOf(
            "Kumar", "Singh", "Sharma", "Yadav", "Verma", "Prasad", "Pandey", "Mishra", "Choudhary", "Paswan",
            "Thakur", "Rai", "Gupta", "Sah", "Kushwaha", "Tiwari", "Sinha", "Manjhi", "Mahto", "Das"
        )
        val skills = listOf(
            "Plumbing", "Electrical", "Carpentry", "Painting", "Masonry",
            "Welding", "AC Repair", "Appliance Repair", "Cleaning", "Construction Labour",
            "Gardening", "Security Guard", "Driver", "General Labour", "Solar Installation"
        )

        val coopDistricts = listOf(
            Triple(1L, "Patna Shramik Sahakari Samiti", Pair(25.6093, 85.1376)),
            Triple(2L, "Muzaffarpur Karigar Sahakari", Pair(26.1209, 85.3647)),
            Triple(3L, "Gaya Nirman Sahakari Samiti", Pair(24.7914, 85.0002)),
            Triple(4L, "Motihari Hunar Sahakari", Pair(26.6469, 84.9089)),
            Triple(5L, "Vaishali Kushal Sahakari Samiti", Pair(25.6858, 85.2152))
        )

        for (i in 0 until 50) {
            val fName = firstNames[i % firstNames.size]
            val lName = lastNames[i % lastNames.size]
            val name = "$fName $lName"
            val skill = skills[i % skills.size]
            val secSkill = skills[(i + 3) % skills.size]
            val coop = coopDistricts[i % coopDistricts.size]
            val expYears = 3 + (i * 7) % 15
            val rating = 4.5f + ((i % 5) * 0.1f)
            val reviewCount = 15 + (i * 9) % 75
            val reliability = 88 + (i * 3) % 12
            val completedJobs = 45 + (i * 18) % 220
            val welfare = 2500.0 + (i * 320.0) % 5000.0
            val dailyEarn = 1200.0 + (i * 180.0) % 1500.0
            val isOnline = (i % 7 != 0) // ~85% online
            val isBusy = (i % 5 == 0) // ~20% currently busy
            val isEmerg = (i % 2 == 0)

            // Scatter latitude and longitude around the cooperative hub
            val latOffset = ((i % 10) - 5) * 0.008
            val lonOffset = (((i * 3) % 10) - 5) * 0.008
            val lat = coop.third.first + latOffset
            val lon = coop.third.second + lonOffset

            val certs = if (expYears > 7) {
                "NSDC Master Technician Level 4, Cooperative Trust Seal, Occupational Health & Safety Certified, State Skill Council Gold Medalist"
            } else {
                "NSDC Certified Level 3, Cooperative Skill Verified, Electrical & Safety Standard Compliant"
            }

            list.add(
                WorkerProfileEntity(
                    id = (i + 1).toLong(),
                    workerCode = "SS-${coop.second.take(3).uppercase()}-2024-${String.format("%03d", i + 1)}",
                    name = name,
                    phone = "+91 9835${String.format("%06d", 100000 + i * 1421)}",
                    email = "${fName.lowercase()}.${lName.lowercase()}@sahakaar.worker.in",
                    cooperativeId = coop.first,
                    cooperativeName = coop.second,
                    primarySkill = skill,
                    secondarySkills = "$secSkill, Tool Handling, Emergency Response",
                    experienceYears = expYears,
                    rating = (rating * 10).toInt() / 10f,
                    reviewCount = reviewCount,
                    reliabilityScore = reliability,
                    isVerified = true,
                    verificationDate = "${10 + (i % 18)} Feb 2024",
                    certList = certs,
                    isOnline = isOnline,
                    isBusy = isBusy,
                    isEmergencyReady = isEmerg,
                    completedJobs = completedJobs,
                    welfareBalance = welfare,
                    dailyEarnings = dailyEarn,
                    hourlyRate = 250.0 + (i % 4) * 50.0,
                    district = coop.second.split(" ").first(),
                    latitude = lat,
                    longitude = lon,
                    profilePhotoUrl = ""
                )
            )
        }
        return list
    }

    fun generateBookings(workers: List<WorkerProfileEntity>): List<BookingEntity> {
        val list = mutableListOf<BookingEntity>()
        val customers = listOf(
            Pair("Rajesh Sharma", "+91 94310 88219"),
            Pair("Priya Verma", "+91 98351 77342"),
            Pair("Dr. Amit Sinha", "+91 94312 99401"),
            Pair("Suman Kumari", "+91 97714 55120"),
            Pair("Vikash Kumar", "+91 93041 33290"),
            Pair("Sunita Devi", "+91 94318 66201"),
            Pair("Anand Jha", "+91 98354 22187"),
            Pair("Kavita Singh", "+91 99340 11928"),
            Pair("Rameshwar Pandey", "+91 94311 44820"),
            Pair("Pooja Mishra", "+91 97710 66319")
        )

        val areas = listOf(
            "Boring Road, Patna", "Kankarbagh Colony, Patna", "Rajendra Nagar, Patna",
            "Bailey Road, Patna", "Sutapatti, Muzaffarpur", "Civil Lines, Gaya",
            "Main Market, Motihari", "Cinema Road, Hajipur", "Ashiana Nagar, Patna", "Danapur Cantt, Patna"
        )

        val problems = listOf(
            Pair("Plumbing", "Main bathroom water pipe burst under the wash basin; severe leakage flooding the floor."),
            Pair("Electrical", "Main MCB switch repeatedly tripping when AC starts; burning smell from panel."),
            Pair("Carpentry", "Wooden wardrobe doors misaligned and hinges loose; lock cylinder jammed."),
            Pair("AC Repair", "Split AC blower running but not cooling; filter cleaned yesterday."),
            Pair("Appliance Repair", "Front-load washing machine drum vibration and error code E2 on spin."),
            Pair("Masonry", "Balcony wall tile cracking and seepage around the drainage point."),
            Pair("Painting", "Living room wall moisture peeling; needs scraping, waterproof putty and 2 coats."),
            Pair("Welding", "Terrace safety railing joint broken due to strong wind; urgent welding."),
            Pair("Cleaning", "Post-renovation full 3BHK flat deep cleaning and floor buffing."),
            Pair("Solar Installation", "1kW rooftop solar inverter showing low voltage error and battery not charging.")
        )

        val statuses = listOf(
            BookingStatus.COMPLETED,
            BookingStatus.IN_PROGRESS,
            BookingStatus.ON_THE_WAY,
            BookingStatus.ARRIVED,
            BookingStatus.ACCEPTED,
            BookingStatus.PENDING,
            BookingStatus.COMPLETED,
            BookingStatus.COMPLETED
        )

        val paymentStatuses = listOf(
            PaymentStatus.PAID_UPI,
            PaymentStatus.PENDING,
            PaymentStatus.PENDING,
            PaymentStatus.PENDING,
            PaymentStatus.PENDING,
            PaymentStatus.PENDING,
            PaymentStatus.PAID_CARD,
            PaymentStatus.PAID_CASH
        )

        for (i in 0 until 52) {
            val cust = customers[i % customers.size]
            val area = areas[i % areas.size]
            val prob = problems[i % problems.size]
            val worker = workers[i % workers.size]
            val status = statuses[i % statuses.size]
            val isEmerg = (i % 6 == 0)
            val payStatus = paymentStatuses[i % paymentStatuses.size]
            val labour = 350.0 + (i % 5) * 75.0
            val material = if (status == BookingStatus.COMPLETED) (i % 4) * 120.0 else 0.0
            val fee = (labour + material) * 0.08
            val total = labour + material + fee

            list.add(
                BookingEntity(
                    id = (i + 1).toLong(),
                    bookingCode = "BK-2026-${String.format("%04d", 8800 + i)}",
                    customerId = "cust_${(i % 10) + 1}",
                    customerName = cust.first,
                    customerPhone = cust.second,
                    serviceName = prob.first,
                    problemDescription = prob.second,
                    address = area,
                    district = area.split(",").last().trim(),
                    isEmergency = isEmerg,
                    status = status,
                    workerId = if (status != BookingStatus.PENDING) worker.id else null,
                    workerName = if (status != BookingStatus.PENDING) worker.name else null,
                    workerPhone = if (status != BookingStatus.PENDING) worker.phone else null,
                    cooperativeId = worker.cooperativeId,
                    cooperativeName = worker.cooperativeName,
                    scheduledDate = "Today, 10:30 AM",
                    scheduledTime = "10:30 AM",
                    labourCost = labour,
                    materialCost = material,
                    platformFee = (fee * 10).toInt() / 10.0,
                    totalAmount = (total * 10).toInt() / 10.0,
                    paymentStatus = payStatus,
                    paymentMethod = when (payStatus) {
                        PaymentStatus.PAID_UPI -> "UPI (PhonePe / GPay)"
                        PaymentStatus.PAID_CARD -> "RuPay Debit Card"
                        PaymentStatus.PAID_CASH -> "Direct Cash"
                        else -> ""
                    },
                    paymentTransactionId = if (payStatus != PaymentStatus.PENDING) "TXN-SS-${100000 + i * 291}" else "",
                    isRated = (status == BookingStatus.COMPLETED && i % 2 == 0),
                    ratingGiven = if (status == BookingStatus.COMPLETED) 4.9f else 0f,
                    createdAt = System.currentTimeMillis() - (i * 3600000L * 6)
                )
            )
        }
        return list
    }

    val welfareTransactions = listOf(
        WelfareTransactionEntity(1, 1, "Cooperative Monthly Welfare Grant", 1500.0, true, "Bihar Labour Welfare Board monthly matching contribution", "01 Sep 2026"),
        WelfareTransactionEntity(2, 1, "Skill Upgradation Incentive", 750.0, true, "Completion of Solar Installation Advanced Certificate", "25 Aug 2026"),
        WelfareTransactionEntity(3, 1, "Group Health Insurance Premium", 450.0, false, "Quarterly premium deduction for Ayushman Bharat & Coop Care", "15 Aug 2026"),
        WelfareTransactionEntity(4, 1, "Job Welfare Cess Deposit", 250.0, true, "5% cooperative welfare share from completed jobs", "10 Aug 2026"),
        WelfareTransactionEntity(5, 1, "Emergency Family Aid", 2000.0, true, "Cooperative welfare emergency medical advance support", "02 Jul 2026"),
        WelfareTransactionEntity(6, 2, "Cooperative Monthly Welfare Grant", 1500.0, true, "State Cooperative development fund contribution", "01 Sep 2026"),
        WelfareTransactionEntity(7, 2, "Safety Equipment Subsidy", 800.0, true, "Insulated toolkit and helmet cooperative rebate", "18 Aug 2026")
    )

    val disputes = listOf(
        DisputeEntity(
            id = 1,
            bookingId = 7,
            bookingCode = "BK-2026-8807",
            customerName = "Rameshwar Pandey",
            workerName = "Santosh Yadav",
            reason = "Partial leak remaining after pipe replacement",
            description = "The joint under the sink started seeping again after 2 hours. Need re-inspection.",
            status = "UNDER_REVIEW",
            adminResponse = "Cooperative technician dispatched for complimentary re-check within 2 hours.",
            createdAt = System.currentTimeMillis() - 86400000L
        ),
        DisputeEntity(
            id = 2,
            bookingId = 15,
            bookingCode = "BK-2026-8815",
            customerName = "Pooja Mishra",
            workerName = "Anil Verma",
            reason = "Difference in billing regarding copper wire material cost",
            description = "Worker charged ₹400 for wire without cooperative bill receipt.",
            status = "RESOLVED",
            adminResponse = "Cooperative reimbursed ₹150 excess charge to customer UPI wallet. Warning issued to worker.",
            createdAt = System.currentTimeMillis() - 172800000L
        )
    )

    val institutionalBookings = listOf(
        InstitutionalBookingEntity(
            id = 1,
            institutionName = "DPS World School Patna",
            institutionType = "School",
            contactPerson = "Col. R. K. Singh (Admin Officer)",
            phone = "+91 94310 11223",
            tradeRequired = "Electrical & Safety Audit",
            workerCount = 8,
            durationDays = 4,
            location = "Khagaul Road, Danapur, Patna",
            status = "ALLOCATED",
            estimatedBudget = 48000.0,
            cooperativeAllocated = "Patna Shramik Sahakari Samiti"
        ),
        InstitutionalBookingEntity(
            id = 2,
            institutionName = "AIIMS Patna Medical College Wing",
            institutionType = "Hospital",
            contactPerson = "Dr. S. K. Narayanan",
            phone = "+91 94312 44556",
            tradeRequired = "Plumbing & Sanitization Overhaul",
            workerCount = 12,
            durationDays = 7,
            location = "Phulwari Sharif, Patna",
            status = "IN_PROGRESS",
            estimatedBudget = 105000.0,
            cooperativeAllocated = "Patna Shramik Sahakari Samiti"
        ),
        InstitutionalBookingEntity(
            id = 3,
            institutionName = "Muzaffarpur Thermal Power Station",
            institutionType = "Factory",
            contactPerson = "Er. Anand Varma (Chief Eng.)",
            phone = "+91 621 229 1100",
            tradeRequired = "Welding & Fabrication",
            workerCount = 15,
            durationDays = 10,
            location = "Kanti, Muzaffarpur",
            status = "ALLOCATED",
            estimatedBudget = 180000.0,
            cooperativeAllocated = "Muzaffarpur Karigar Sahakari"
        ),
        InstitutionalBookingEntity(
            id = 4,
            institutionName = "Hotel Maurya Patna",
            institutionType = "Hotel",
            contactPerson = "General Manager B. Sengupta",
            phone = "+91 612 220 1000",
            tradeRequired = "Carpentry & Polishing",
            workerCount = 6,
            durationDays = 5,
            location = "South Gandhi Maidan, Patna",
            status = "PENDING",
            estimatedBudget = 42000.0,
            cooperativeAllocated = "Patna Shramik Sahakari Samiti"
        )
    )

    val demandForecasts = listOf(
        DemandForecastEntity(
            id = 1,
            serviceName = "Plumbing",
            zone = "Zone 1 - Patna West (Kankarbagh / Danapur)",
            predictedDemand = 145,
            availableWorkers = 92,
            shortage = 53,
            recommendation = "Mobilize 50+ plumbing apprentices from Muzaffarpur cooperative and authorize overtime bonus."
        ),
        DemandForecastEntity(
            id = 2,
            serviceName = "Electrical",
            zone = "Zone 2 - Patna Central (Boring Rd / Bailey Rd)",
            predictedDemand = 180,
            availableWorkers = 140,
            shortage = 40,
            recommendation = "Deploy additional electricians to standby hubs near Dak Bungalow for 15-min emergency response."
        ),
        DemandForecastEntity(
            id = 3,
            serviceName = "AC Repair",
            zone = "Zone 3 - Muzaffarpur Urban",
            predictedDemand = 95,
            availableWorkers = 58,
            shortage = 37,
            recommendation = "Conduct 2-day refresher workshop on inverter AC gas refill; allocate emergency travel allowance."
        ),
        DemandForecastEntity(
            id = 4,
            serviceName = "Masonry & Tiling",
            zone = "Zone 4 - Gaya Heritage & Civil Lines",
            predictedDemand = 110,
            availableWorkers = 88,
            shortage = 22,
            recommendation = "Partner with Gaya Nirman Sahakari to onboard 20 rural mason trainees with digital skill passports."
        )
    )

    val notifications = listOf(
        NotificationEntity(1, "CUSTOMER", "Worker Assigned", "Raj Kumar (Plumber, ⭐4.9) has been assigned to your booking BK-8801.", "5m ago", false, 1),
        NotificationEntity(2, "CUSTOMER", "Worker On The Way", "Your electrician Amit Singh is 1.8 km away. ETA 12 mins.", "15m ago", false, 2),
        NotificationEntity(3, "WORKER", "New Emergency Job!", "Burst pipe reported at Bailey Road (2.2 km away). Tap to Accept.", "2m ago", false, 6),
        NotificationEntity(4, "WORKER", "Payment Received", "₹480 credited to your daily wallet for booking BK-8802.", "1h ago", true, 2),
        NotificationEntity(5, "COOPERATIVE_ADMIN", "Emergency Surge Alert", "High demand detected in Patna Central: 14 urgent plumbing requests in 30 mins.", "10m ago", false, null),
        NotificationEntity(6, "COOPERATIVE_ADMIN", "New Dispute Raised", "Customer raised dispute for BK-8807. Action required.", "4h ago", false, 7)
    )
}
