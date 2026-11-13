```sql pensioners migration
with data as(
	select P_No, Scheme from(
		select P_No, Scheme,
		ROW_NUMBER() over(partition by P_No order by TranDate desc) as rn
		from pentrans
	) t
	where t.rn = 1
)
select
ROW_NUMBER() over(order by date_eng) as id,
'root' as created_by,
date_eng as created_date,
'root' as modified_by,
date_eng as modified_date,
citizenship as citizenship_no,
replace(date_nep, '.', '-') as date_nep,
case festivalmonth when 'ASOJ' then 6 when 'BHADRA' then 5 else 6 end as festival_month, -- new db has this data in numeric form
Insurance as insurance_amount,
case increment when 'Yes' then 1 when 'Fixed' then 2 else 2 end as is_increment,
case Main_Bank_Code when 2 then 3 when 3 then 4 else 0 end as main_bank_code, -- matching with codes in new db
1 as message_medium, -- 1 = SMS
nationalid as national_id,
1 as national_id_status, -- 1 = verified
'6' as other_type_code, -- 6 = सामान्य
panno as pan_number,
P_Account_No as pension_account_no,
P_Address as pension_address,
Balance as pension_amount,
P_Amount_Per_Months as pension_amount_per_month,
bank_code as pension_bank_code,
P_Catagory_Code as pension_category_code,
replace(P_Date_Nep, '.', '-') as pension_date_nep,
replace(P_Death_Date_Nep, '.', '-') as pension_death_date_nep,
m.district as pension_district,
replace(P_DOB_Nep, '.', '-') as pension_dob_nep,
null as pension_email, -- new field in new database,
replace(P_End_date_Nep, '.', '-') as pension_end_date_nep,
P_Sex as pension_gender,
100 as pension_increment_percentage,
P_Last_Set_Amount as pension_last_set_amount,
replace(P_Last_Set_Date, '.', '-') as pension_last_set_date,
P_Level_code as pension_level_code,
case Married when 1 then 1 when 0 then 2 end as pension_marital_status,
P_Name_Eng as pension_name_eng,
P_Name_Nep as pension_name_nep,
P_Sub_Patta_No as pension_no,
replace(P_Paid_Upto, '.', '-') as pension_paid_up_to,
P_Phone_No as pension_phone_no,
P_Post_Code as pension_post_code,
d.province as pension_province,
P_Receive_Percentage as pension_receive_percentage,
P_Relation_Code as pension_relation,
P_Remarks as pension_remarks,
replace(P_Retair_Nep, '.', '-') as pension_retire_nep,
P_Retair_Office as pension_retire_office,
dd.Scheme as pension_scheme_code, -- voucher table ko latest scheme rakhne
case when P_Pention_Type = 29 then 4 when p_data_edit = 1 then 6 else P_Status end as pension_status,
p_Sub_name_Eng as pension_sub_name_eng,
P_Sub_Name as pension_sub_name_nep,
m.P_No as pension_subpno,
P_Sub_Pensioner as pension_sub_pensioner,
P_Type_Code as pension_type_code,
replace(presentdate, '.', '-') as present_date,
'' as sanket_no, -- no sanket_no info
0 as stage, -- new field in new db
vdc as vdc,
0 as virtual_presence_status, -- new field
ward as ward,
replace(Year60Date, '.', '-') as year60date,
replace(Year75Date, '.', '-') as year75date,
replace(Year85Date, '.', '-') as year85date
into temp_pensioners_mig
from Pension_Master m
left join Pension_District_Master d on d.id = m.district
left join data dd on dd.P_No = m.P_No
where m.SeqNo not in (
	select Max(SeqNo) as SeqNo
	from Pension_Master
	group by P_No, P_Sub_Patta_No
	having count(1) > 1
);

-- update national-id
UPDATE pensioner_registration
SET national_id =
    SUBSTRING(national_id FROM 1 FOR 3) || '-' ||
    SUBSTRING(national_id FROM 4 FOR 3) || '-' ||
    SUBSTRING(national_id FROM 7 FOR 3) || '-' ||
    SUBSTRING(national_id FROM 10 FOR 1)
WHERE LENGTH(national_id) = 10;

-- pension death date update
select pension_no, max(pension_death_date_nep) as pension_death_date_nep
into temp_pensioner_death_dates
from pensioner_registration
where pension_sub_pensioner = 1
	and pension_death_date_nep is not null
	and pension_death_date_nep <> ''
group by pension_no

create unique index idx_one on temp_pensioner_death_dates (pension_no);

update pensioner_registration m
set pension_death_date_nep = coalesce((
	select pension_death_date_nep
	from temp_pensioner_death_dates
	where pension_no = m.pension_no
), null)
where m.pension_sub_pensioner = 0 and (m.pension_death_date_nep is null or m.pension_death_date_nep = '');

update pensioner_registration
set pension_death_date_nep = null
where pension_sub_pensioner = 1;

-- tameli pensioners
SELECT P_No into temp_tameli_pensioners_mig
from Pension_Master where P_Pention_Type = 29;

create table temp_tameli_pensioners (
	p_no varchar(100) primary key
);

update pensioner_registration
set pension_status = 4
where pension_subpno in (select p_no from temp_tameli_pensioners);
```

```sql voucher_transaction_master
select
ID as id,
'root' as created_by,
'2026-02-04' as created_date,
'root' as modified_by,
'2026-02-04' as modified_date,
'3' as calculation_type_code, -- 3 = other (old db has no field)
replace(EndDate, '.', '-') as end_date,
4 as stage,
replace(StartDate, '.', '-') as start_date,
replace(TranDate, '.', '-') as statement_date,
2 as status,
TranID as voucher_no,
case
	when Particulars COLLATE Latin1_General_CS_AS in ('Additional Payment-Adjustment',
		'Additional Payment-Voucher',
		'Additional Transaction',
		'Over Payment-Adjustment',
		'Over Payment-voucher')
	then 1
	when Particulars COLLATE Latin1_General_CS_AS in ('MONTHLY PENSION') then 2
	when Particulars COLLATE Latin1_General_CS_AS in ('INCREAMENT', 'INCREAMENT 2079/04', 'INCREAMENT 2079/05') then 3
	else 4
end as voucher_type,
Main_Bank_Code as main_bank_code,
coalesce(Balance, 0) as additional_pay_amount,
null as year60date,
null as year75date,
null as year85date,
TranAmount as calculation_amount,
replace(StartDate, '.', '-') as calculation_from_date,
replace(EndDate, '.', '-') as calculation_to_date,
last_set_amount as new_pension_amount,
replace(LastSetEndDate, '.', '-') as new_pension_end_date,
replace(Last_set_date, '.', '-') as new_pension_start_date,
scheme as new_scheme_code,
'' as nominee,
'' as payment_from_bank_ac,
Remark as remarks,
STATMENT_AMOUNT as statement_amount,
Tax as tax,
deduction as total_adjustment_amt,
payment_to_other as total_payment_amt,
Transfer_to_govt as transfer_to_govt_adj,
null as forward_id,
'' as note,
'' as user_ids,
P_No as pension_subpno,
'' as scheme_code, -- no corresponding field
case
		when TranDate between '2075.04.01' and '2076.03.32' then '2075/76'
		when TranDate between '2076.04.01' and '2077.03.32' then '2076/77'
		when TranDate between '2077.04.01' and '2078.03.32' then '2077/78'
		when TranDate between '2078.04.01' and '2079.03.32' then '2078/79'
		when TranDate between '2079.04.01' and '2080.03.32' then '2079/80'
		when TranDate between '2080.04.01' and '2081.03.32' then '2080/81'
		when TranDate between '2081.04.01' and '2082.03.32' then '2081/82'
		when TranDate between '2082.04.01' and '2083.03.32' then '2082/83'
end
as fiscal_year,
'' as ref_batch_no,
PensionType as pensioner_type_code,
Particulars as particulars,
amount as amount
into temp_pentrans_mig
from pentrans;

-- creating chunks for migration
select *
into temp_pentrans_mig_1
from temp_pentrans_mig
order by id
offset 0 rows
fetch next 2000000 rows only; -- 20 lakh

-- in case of primary key value violation (same id)
--delete from voucher_transaction_master
--where id in (
--select id from voucher_transaction_master
--order by id desc
--limit 800000
--)
```

```sql voucher_transaction_details
select
row_number() over(order by d.id) as id,
'root' as created_by,
'2026-02-01' as created_date,
'root' as modified_by,
'2026-02-01' as modified_date,
m.ID as master_id,
replace(d.from_date, '.', '-') as start_date,
replace(d.to_date, '.', '-') as end_date,
d.receivable_amount as receivable_amount,
d.receive_amount as receive_amount,
d.difference_amount as difference,
0 as tax, -- no tax info found
d.TOTAL_DIFFERENCE as total_amount,
d.increament as increment,
case d.PERIOD_TYPE when 'D' then 1 when 'M' then 2 end as period_type,
d.PERIOD as period,
case
		when m.TranDate between '2075.04.01' and '2076.03.32' then '2075/76'
		when m.TranDate between '2076.04.01' and '2077.03.32' then '2076/77'
		when m.TranDate between '2077.04.01' and '2078.03.32' then '2077/78'
		when m.TranDate between '2078.04.01' and '2079.03.32' then '2078/79'
		when m.TranDate between '2079.04.01' and '2080.03.32' then '2079/80'
		when m.TranDate between '2080.04.01' and '2081.03.32' then '2080/81'
		when m.TranDate between '2081.04.01' and '2082.03.32' then '2081/82'
		when m.TranDate between '2082.04.01' and '2083.03.32' then '2082/83'
end
as fiscal_year,
3 as status, -- 3 = payment success
d.PENSION_ID as pension_subpno
into temp_transaction_detail_mig
from TRANSACTION_DETIAL d
inner join pentrans m on m.TranID = d.TRANDI
where m.particulars in ('Additional Payment-Adjustment', 'Additional Payment-Voucher');

-- generating missing voucher_transaction_details
merge into voucher_transaction_details d
using voucher_transaction_master m on (m.id = d.master_id)
when not matched then
insert (
	created_by,
	created_date,
	modified_by,
	modified_date,
	difference,
	end_date,
	increment,
	master_id,
	pension_subpno,
	period,
	period_type,
	receivable_amount,
	receive_amount,
	start_date,
	total_amount,
	tax,
	fiscal_year,
	status
)
values(
	m.created_by,
	m.created_date,
	m.modified_by,
	m.modified_date,
	case m.voucher_type when 2 then m.amount else 0 end, -- difference
	m.end_date,
	case m.voucher_type when 3 then m.amount else 0 end, -- increment
	m.id,
	m.pension_subpno,
	0,
	0,
	case m.voucher_type when 2 then m.amount else 0 end, -- receivable amount
	0,
	m.start_date,
	m.amount, -- total amount
	case when m.tax is null then 0 else m.tax end, -- tax
	m.fiscal_year,
	3
)
```

```sql presence migration
select
row_number() over(order by TranDate) as id,
User_Name as created_by,
'2026-02-01' as created_date,
User_Name as modified_by,
'2026-02-01' as modified_date,
null as file_data,
NationalId as nid,
p_no as pension_subpno,
replace(presentdate, '.', '-') as present_date,
remarks as remarks,
1 as stage,
1 as status,
1 as type,
'' as present_date_eng,
null as content_type
into temp_pension_presentdate_mig
from Pension_PresentDate
where status_type = 'Presence';


update pensioner_presence
set present_date_eng = (select roman_date from nepali_calendar where nepali_date = present_date)
where present_date_eng is null;
```

```sql bank-accounts migration
select
row_number() over(order by TranDate) as id,
User_Name as created_by,
'2026-02-15' as created_date,
User_Name as modified_by,
'2026-02-15' as modified_date,
accountno as account_no,
p_no as pension_subpno,
replace(presentdate, '.', '-') as registration_date,
1 as stage,
1 as status
into temp_bank_accounts_mig
from Pension_PresentDate
where status_type = 'A/C Open';
```

```sql after migration
VACUUM ANALYZE voucher_transaction_master;
VACUUM ANALYZE voucher_transaction_details;

REINDEX TABLE voucher_transaction_master;
REINDEX TABLE voucher_transaction_details;
```

```increment migration
select
23273 + row_number() over(order by id) as id,
'root' as created_by,
'2026-08-10' as created_date,
'root' as modified_by,
'2026-08-10' as modified_date,
cast(p_cat_code as varchar(100)) as category_code,
p_increse_amount_general as increment_amount,
7481 + row_number() over(order by id) as increment_code,
replace(p_increse_start_date, '.', '-') as increment_date,
cast(p_level_code as varchar(100)) as level_code,
cast(id as varchar(100)) as old_id,
cast(p_post_code as varchar(100)) as post_code,
cast(p_scheme_code as varchar(100)) as scheme_code,
1 as tatus,
cast(p_type_code as varchar(100)) as type_code
into temp_increments_mig
from Pension_Increse_Master
where p_increse_start_date = '2083.04.01';
```
