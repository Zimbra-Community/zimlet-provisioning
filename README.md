# Zimlet Provisioning
This is an extension that allows you to set zimbraZimletUserProperties LDAP values via REST API. This is useful if you do not have access to CLI or LDAP on your server, but also as setting individual Zimlet properties on the CLI is very hard, as this is not really supported using normal LDIF syntax or zmprov.

This extension only supports setting zimbraZimletUserProperties, and only one of them for a single user at a time.

### Installing

     mkdir /opt/zimbra/lib/ext/externalProvision
     cd /opt/zimbra/lib/ext/externalProvision
     wget https://github.com/Zimbra-Community/zimlet-provisioning/raw/master/extension/out/artifacts/externalProvision_jar/externalProvision.jar -O /opt/zimbra/lib/ext/externalProvision/externalProvision.jar
     
Then set the security preferences using `nano /opt/zimbra/lib/ext/externalProvision/config.properties`, example:
     
      secret=fo4So5cheez0gul7yee7phoosh0voh2Eitao6wee
      allowedProps=com_examplestuff_zimlet
      allowedIps=*

Secret must be at least 9 characters long, but I recommend 40 or so characters Don't forget `zmmailboxdctl restart`.

allowedProps support a comma separated string of allowed properties, here is how to get them:

      zmprov -l ga admin@zimbradev.barrydegraaff.tk zimbraZimletUserProperties
      # name admin@zimbradev.barrydegraaff.tk
      zimbraZimletUserProperties: tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_username:admin
      zimbraZimletUserProperties: tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_password:
      zimbraZimletUserProperties: tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_use_numbers:false
      zimbraZimletUserProperties: tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_server_port:443
      zimbraZimletUserProperties: tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_server_path:/nextcloud/remote.php/webdav/
      zimbraZimletUserProperties: tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_server_name:https://zimbradev
      zimbraZimletUserProperties: tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_oc_folder:/nextcloud
      zimbraZimletUserProperties: tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_template:
      zimbraZimletUserProperties: com_examplestuff_zimlet:com_examplestuff_properties:{"Username":"test45@zimbradev.barrydegraaff.tk","Password":"dapassword","DelaySend":12}
      

Now if you want to provision `owncloud_zimlet_password`, you must add to the config.properties: `tk_barrydegraaff_owncloud_zimlet:owncloud_zimlet_password`. Or some Zimlets may use JSON, if you want to set `com_examplestuff_zimlet` you must configure `com_examplestuff_zimlet` in the config.properties. TEST IN A TEST ENVIRONMENT FIRST! As it is easy to overwrite stuff unintentional. E.g. one cannot use `tk_barrydegraaff_owncloud_zimlet` as a single request would remove all matches of `tk_barrydegraaff_owncloud_zimlet` and only put one back!

You can add more security by restricting the IP's that are allowed to call this API using a comma separated list in `allowedIps` or * to allow from all.

### Example API call

The call needs to contains the secret, the property you are changing, the value to set and the username you want to set the zimbraZimletUserProperties for.

      curl -k -d 'secret=fo4So5cheez0gul7yee7phoosh0voh2Eitao6wee&property=com_examplestuff_zimlet&username=admin@zimbradev.barrydegraaff.tk&value=com_examplestuff_properties:{"Username":"test45@zimbradev.barrydegraaff.tk","Password":"dapassword","DelaySend":12}' -H "Content-Type: application/x-www-form-urlencoded" -X POST https://your-zimbra-server/service/extension/externalprovision -v

### It takes some time

If you call the API before the user logs on for the first time, that user will see/use the values set via the API immediate. If you update existing users, it takes a while (Zimbra cache) before the user sees/uses the new values. You can restart mailbox to make the change immediate or try an find our how Zimbra cache works (good luck with that).


### Donations

If you find it useful and want to support continued development, you can make donations via:
- PayPal: info@barrydegraaff.tk
- Bank transfer: IBAN NL55ABNA0623226413 ; BIC ABNANL2A



