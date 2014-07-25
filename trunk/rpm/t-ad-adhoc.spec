Name: t-ad-adhoc
Version:1.0.318
Release: %(echo $RELEASE)%{?dist}

Summary:t_ad_adhoc
URL: http://svn.simba.taobao.com/svn/DW/data_bj/P4P/adhoc/higo/trunk/
Group: taobao/ad
License: Commercial

BuildArch: noarch
%description
t_ad_adhoc
%{_svn_path}
%{_svn_revision}

%define _prefix /home/taobao/bluewhale/

%build


%install

mkdir -p .%{_prefix}

cp $OLDPWD/../target/alimama-adhoc.tar.gz .%{_prefix}/alimama-adhoc.tar.gz


%files
%defattr(755,taobao,users)
%attr(0755,taobao,users) %{_prefix}
%attr(0755,taobao,users) %{_prefix}/alimama-adhoc.tar.gz

%{_prefix}

%post
