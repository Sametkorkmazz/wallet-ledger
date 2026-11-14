import SignInForm from "@/components/auth/SignInForm";
import SignUpForm from "@/components/auth/SignUpForm";
import ThemeToggle from "@/components/ThemeToggle";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Check, Wallet } from "lucide-react";
import { useLocation, useNavigate } from "react-router-dom";

const highlights = [
  "Every transfer has a unique reference, so a retry never pays twice.",
  "Balances come from an append-only ledger that always sums to zero.",
  "All money in this app is fake.",
];

export default function AuthPage() {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const tab = pathname === "/signup" ? "signup" : "signin";

  return (
    <main className="min-h-screen bg-background relative flex flex-wrap items-center justify-center gap-12 p-8">
      <div className="absolute right-4 top-4">
        <ThemeToggle />
      </div>
      <div className="max-w-md flex flex-col gap-5">
        <div className="flex items-center gap-2">
          <span className="flex size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <Wallet className="size-5" />
          </span>
          <span className="font-medium">Wallet &amp; Ledger</span>
        </div>
        <h1 className="text-[40px] tracking-tight leading-12 font-bold">
          Hold EUR, USD and TRY. Send money to other users.
        </h1>
        <ul className="mt-3 flex flex-col gap-3 text-muted-foreground">
          {highlights.map((text) => (
            <li key={text} className="flex items-start gap-3">
              <Check className="size-6 shrink-0 rounded-full bg-primary/12 p-1 text-primary" />
              <span>{text}</span>
            </li>
          ))}
        </ul>
      </div>

      <Card className="w-full max-w-md shadow-none">
        <CardContent className="flex flex-col gap-5">
          <Tabs
            value={tab}
            onValueChange={(v) =>
              navigate(v === "signup" ? "/signup" : "/login")
            }
            className="flex-col"
          >
            <TabsList className="grid w-full grid-cols-2 group-data-horizontal/tabs:h-12">
              <TabsTrigger
                value="signin"
                className="text-base data-active:border-border"
              >
                Sign in
              </TabsTrigger>
              <TabsTrigger
                value="signup"
                className="text-base data-active:border-border"
              >
                Create account
              </TabsTrigger>
            </TabsList>
            <TabsContent value="signin" className="flex flex-col gap-4 pt-4">
              <div>
                <h2 className="text-2xl font-semibold tracking-tight">
                  Welcome back
                </h2>
                <p className="text-sm text-muted-foreground">
                  Sign in to see your balances and send money.
                </p>
              </div>
              <SignInForm />
            </TabsContent>
            <TabsContent value="signup" className="flex flex-col gap-4 pt-4">
              <div>
                <h2 className="text-2xl font-semibold tracking-tight">
                  Create your account
                </h2>
                <p className="text-sm text-muted-foreground">
                  Start with fake money in EUR, USD and TRY.
                </p>
              </div>
              <SignUpForm />
            </TabsContent>
          </Tabs>

          <div className="flex items-center gap-3 text-sm text-muted-foreground">
            <Separator className="flex-1" />
            OR
            <Separator className="flex-1" />
          </div>

          <Button
            variant="outline"
            size="lg"
            className="w-full"
            onClick={() => navigate("/dashboard")}
          >
            Try demo account
          </Button>
        </CardContent>
      </Card>
    </main>
  );
}
